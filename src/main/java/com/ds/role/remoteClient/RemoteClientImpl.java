package com.ds.role.remoteClient;

import com.ds.pojo.Item;
import com.ds.pojo.User;
import com.ds.role.remoteServer.RemoteServer;
import com.ds.utils.AuctionTimer;
import com.ds.utils.StringBuilder;

import com.ds.utils.GetConnection;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.ds.utils.Encoding.encodePassword;

public class RemoteClientImpl implements RemoteClient {
    public static List<Item> auctionItem;
    public static List<String> registeredUsers;

    // the current contact method
    public static String highest_bidder_contact_info;
    // hadoop file system
    private static FileSystem fs;
    // sql connection
    private static final Connection connection = GetConnection.getConnection();
    // read and write lock
    private final ReentrantReadWriteLock lock;

    // remaining time for auction
    private static AuctionTimer auctionTimer;
    // a flag to check whether the server has connection or not
    private static boolean hasConnection = false;

    public RemoteClientImpl() throws IOException, URISyntaxException, InterruptedException {
        auctionItem = new LinkedList<>();
        registeredUsers = new LinkedList<>();
        initHDFSConnection();
        lock = new ReentrantReadWriteLock();
    }

    public RemoteClientImpl(AuctionTimer auction_timer) throws IOException, URISyntaxException, InterruptedException {
        this();
        auctionTimer = auction_timer;
    }

    // get the bidding price of the current item
    @Override
    public float getCurrentBiddingPrice() throws RemoteException {
        this.acquireReadLock();
        float bidding_price = auctionItem.get(0).getBidding_price();
        this.releaseReadLock();
        return bidding_price;
    }

    @Override
    public Item getItemDetails() throws RemoteException {
        this.acquireReadLock();
        Item result = auctionItem.get(0);
        this.releaseReadLock();
        return result;
    }

    @Override
    public boolean updateBiddingPrice(String contact_info, float biddingPrice) throws IOException, NotBoundException {
        // write lock
        this.acquireWriteLock();
        String[] data = contact_info.split("/");
        String ip = data[2];
        String user = data[3];
        if (biddingPrice > auctionItem.get(0).getBidding_price()) {
            // successful
            auctionItem.get(0).setBidding_price(biddingPrice);
            // the contact info of the highest bidder
            highest_bidder_contact_info = contact_info;
            auctionItem.get(0).setHighest_bidder(user);

            // write the newest information on the local Excel file
            updateLocalFile(auctionItem.get(0));

            // release the lock
            this.releaseWriteLock();

            String successMessage = "you have successfully bid for the item " + auctionItem.get(0).getName() +
                    " with the price " + biddingPrice;
            sendMessage2Client(contact_info, successMessage);

            // process the updated information
            processUpdate(user, biddingPrice);
            return true;
        } else {
            // fail
            String errorMessage = "your bidding price isn't larger than the current highest bidding price";
            sendMessage2Client(contact_info, errorMessage);
            this.releaseWriteLock();
            return false;
        }
    }

    // get the current highest bidder of the item
    @Override
    public String getHighestBidder() throws RemoteException {
        this.acquireReadLock();
        String bidder =  auctionItem.get(0).getHighest_bidder();
        this.releaseReadLock();
        return bidder;
    }

    // authenticate the user
    @Override
    public boolean authenticateUser(String email, String password) throws RemoteException, UnsupportedEncodingException, NoSuchAlgorithmException, SQLException, MalformedURLException, NotBoundException {
        return authenticate(email, password);
    }

    // register the new account
    @Override
    public boolean registerUser(String email, String password) throws RemoteException, SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        return register(email, password);
    }

    // add the current client into the server system
    @Override
    public void addClient(String contact_info) throws RemoteException, NotBoundException {
        registeredUsers.add(contact_info);
        if (hasConnection == false) {
            hasConnection = true;
            auctionTimer.start();
        }
        sayHello(contact_info);
    }

    // return all the items listed in the server
    @Override
    public List<Item> getAllItems() {
        return auctionItem;
    }

    // get the item at index selectedIndex
    @Override
    public Item getItemAt(int selectedIndex) throws RemoteException {
        return auctionItem.get(selectedIndex);
    }

    // log out
    @Override
    public boolean logOut(String contact_info) throws RemoteException {
        for (int i = 0; i < registeredUsers.size(); i++){
            if (contact_info.equals(registeredUsers.get(i))) {
                registeredUsers.remove(i);
                return true;
            }
        }
        return false;
    }

    // upload the current file to the cloud
    private static void uploadFileToCloud(String filename) throws IOException {
        /*
        the first parameter: delete the original data
        the second parameter: allow to overwrite
        the third parameter: path of the file on the local computer
        the fourth parameter: destination path on the cloud
        */
        String localPath = "src/data/" + filename + ".csv";
        String targetPath = "/bidding_data/" + filename + "/" + filename + ".csv";
        fs.copyFromLocalFile(false, true, new Path(localPath), new Path(targetPath));
    }

    // create the directory on a given path on hadoop filesystem
    private static void createDirOnCloud(String path) throws IOException {
        fs.mkdirs(new Path(path));
    }

    // create the Excel file on server local system
    private static void createExcelOnLocal(Item item) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // create the first row
        XSSFRow row=sheet.createRow(0);
        String[] title={"name","starting price","current bidding price","brand","highest bidder", "status", "time"};

        for (int i = 0;i < title.length;i++){
            XSSFCell cell_title = row.createCell(i);
            cell_title.setCellValue(title[i]);
        }

        XSSFRow row1=sheet.createRow(1);
        XSSFCell cell_content = null;
        String[] row_data = {item.getName(), String.valueOf(item.getStarting_price()), "null",
                item.getBrand(), "null", "create", getTime()};

        for (int i = 0; i < title.length; i++) {
            cell_content = row1.createCell(i);
            cell_content.setCellValue(row_data[i]);
        }

        String filePath = "src/data/" + item.getName() + ".csv";
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream stream = FileUtils.openOutputStream(file);
        workbook.write(stream);
        stream.close();
    }

    // process the update when a new bidder bids for the item successfully
    private void processUpdate(String email, float biddingPrice) throws IOException, NotBoundException {
        // broadcast the message
        String msg = "the countdown timer for auction has been reset because of the new bidding from " + email;

        broadcast(msg);

        // reset the timer back
        auctionTimer.reset();
    }

    private static void updateLocalFile(Item item) throws IOException {
        String path = StringBuilder.stringBuilder(StringBuilder.excelLocal, item.getName()).concat(".csv");
        FileInputStream fis = new FileInputStream(new File(path));

        XSSFWorkbook wb=new XSSFWorkbook(fis);
        XSSFSheet sheet=wb.getSheetAt(0);

        FileOutputStream out=new FileOutputStream(path);
        XSSFRow row =sheet.createRow((short)(sheet.getLastRowNum()+1));

        String[] row_data = {item.getName(), String.valueOf(item.getStarting_price()), String.valueOf(item.getBidding_price()),
                item.getBrand(), item.getHighest_bidder(), "update", getTime()};

        XSSFCell cell_content = null;
        for (int i=0; i < row_data.length; i++) {
            cell_content = row.createCell(i);
            cell_content.setCellValue(row_data[i]);
        }

        out.flush();
        wb.write(out);
        out.close();
        fis.close();
    }

    private static boolean authenticate(String email, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException, MalformedURLException, NotBoundException, RemoteException {
        Connection connection = GetConnection.getConnection();
        String encoded = encodePassword(password);
        PreparedStatement pst = connection.prepareStatement("select * from user where email=?");
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        User result = new User();
        while(rs.next()) {
            result.setPassword(rs.getString("password"));
        }
        boolean res = encoded.equals(result.getPassword());
        if (res == true) {
            // authenticate success
            System.out.println("user: " + email + " has login into the system");
        }
        return res;
    }

    private static boolean register(String email, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException {
        String encoded = encodePassword(password);
        PreparedStatement pst = connection.prepareStatement("insert into user(email, password) values(?, ?)");
        pst.setString(1, email);
        pst.setString(2, encoded);
        int status = pst.executeUpdate();
        if (status == 1) {
            // successfully register a new account
            System.out.println("successfully register a new account");
            return true;
        } else {
            System.out.println("fail to register a new account");
            return false;
        }
    }

    // used to initialize the hadoop filesystem
    private static void initHDFSConnection() throws URISyntaxException, IOException, InterruptedException {
        // address of the connected cluster
        URI uri = new URI("hdfs://hadoop102:8020");

        // create a configuration file
        Configuration configuration = new Configuration();

        // set the username used to connect hadoop filesystem
        String user = "eric";

        // get the instance of hadoop filesystem
        fs = FileSystem.get(uri, configuration, user);
    }

    // close the hadoop filesystem connection
    private static void closeHDFSConnection() throws IOException {
        fs.close();
    }

    // make directory on hadoop file system
    private static void mkdirOnCloud(String path) throws IOException {
        fs.mkdirs(new Path(path));
    }

    // get the current time on server
    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String dateStr = sdf.format(date);
        return dateStr;
    }

    // broadcast the message to each registered user
    public static void broadcast(String msg) throws MalformedURLException, NotBoundException, RemoteException {
        System.out.println("is broadcasting");
        System.out.println("current number of users: " + registeredUsers.size());
        System.out.println("the message is: " + msg);
        for (String client: registeredUsers) {
            String[] data = client.split("/");
            String ip = data[2];
            String user = data[3];
            RemoteServer rs = (RemoteServer) LocateRegistry.getRegistry(ip, 1099).lookup(client);
            rs.messageFromServer(msg);
        }
    }

    // server sends the message to the client
    public static void sendMessage2Client(String contact_info, String msg) throws RemoteException, NotBoundException {
        String[] data = contact_info.split("/");
        String ip = data[2];
        String user = data[3];
        System.out.println("ip: " + ip);
        RemoteServer rs = (RemoteServer) LocateRegistry.getRegistry(ip, 1099).lookup(contact_info);
        rs.messageFromServer(msg);

    }

    // process the info
    public static void processFinalBid() throws IOException, NotBoundException {
        if (auctionItem.get(0).getHighest_bidder() != null) {
            String[] data = highest_bidder_contact_info.split("/");
            String ip = data[2];
            String user = data[3];
            RemoteServer rs = (RemoteServer) LocateRegistry.getRegistry(ip, 1099).lookup(highest_bidder_contact_info);
            String successMsg = "You have successfully auctioned the item!";
            rs.messageFromServer(successMsg);

            uploadFileToCloud(auctionItem.get(0).getName());

            Item item = auctionItem.get(0);

            auctionItem.remove(0);

            // all the items are sold out
            if (auctionItem.size() == 0) {
                String msg = "all the items have been sold out. Thank you for your participating!";
                broadcast(msg);

                // cancel all the timers
                auctionTimer.timerTask.cancel();
                auctionTimer.countDownTimer.timerTask.cancel();
            } else {
                // continue the auction, start the next item
                String msg = "the item " + item.getName() + " has been sold to " + item.getHighest_bidder()
                        + "; the next item is: " + auctionItem.get(0).getName();
                broadcast(msg);
                auctionTimer.start();
            }
        } else {
            // item passes, send it to the end of the queue
            String errorMsg = "The item is passed, it will be auctioned again later";
            broadcast(errorMsg);
            auctionItem.add(auctionItem.size(), auctionItem.get(0));
            auctionItem.remove(0);

            // update the next item
            String msg = "the next item is: " + auctionItem.get(0).getName();
            auctionTimer.start();
        }
    }

    // the server sends hello message to the client
    private static void sayHello(String contact_info) throws NotBoundException, RemoteException {
        String[] data = contact_info.split("/");
        String ip = data[2];
        String user = data[3];
        String message = "hello: " + user + "! " + "The current auction item is: " + auctionItem.get(0).getName()
                + "; ";
        RemoteServer rs = (RemoteServer) LocateRegistry.getRegistry(ip, 1099).lookup(contact_info);
        rs.messageFromServer(message);
    }

    private void acquireReadLock() {
        if (lock.readLock().tryLock()) {
            System.out.println("is fetching read lock");
        }
    }

    private void releaseReadLock() {
        lock.readLock().unlock();
        System.out.println("is releasing the right");
    }

    private void acquireWriteLock() {
        if (lock.writeLock().tryLock()) {
            System.out.println("is fetching write lock");
        }
    }

    private void releaseWriteLock() {
        lock.writeLock().unlock();
        System.out.println("is releasing write lock");
    }

    public void addItem(Item newItem) throws IOException {
        auctionItem.add(auctionItem.size(), newItem);
        createExcelOnLocal(newItem);
        createDirOnCloud(StringBuilder.stringBuilder(StringBuilder.excelRoot, newItem.getName()));
    }
}

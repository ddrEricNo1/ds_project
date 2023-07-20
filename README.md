# This is a repo for my distributed system final project
## updated at 2023/7/1
## Key techniques include:
* **Hadoop** for data replicas and backup
* **RMI** for remote connection
* **mySQL** for username and password storage locally
* **ReentrantReadWriteLock** for read and write lock and consistency
* **JFrame** for simple GUI design 
* * *

## Functionalities
> This is a client-server archtecture distributed system for auction, clients are the bidders who bid for the system. The functionalities for bidders are listed as follows:
> 1. view the current item details(the current bidding price, name, brand)
> 2. bid for the item
> 3. log out the system

> For the server, the functionalities are listed as follows:
> 1. **process the bidding** (check the current bidding price with the highest bidding price, bidding logic)
> 2. **Timer for auction**. 1 Minute for auction for each item. When a bidder has successfully bid for the system, the timer will be reset to 1 minute.
> 3. **Timer for message**. The server will send a message to all the clients every 20 seconds to remind them of the remaining time for the current item's auction. 
> 4. **Confirm the transaction**. After 1-min timer ends, the system will confirm the transaction and gives the item to the highest bidder.
> 5. **Sending public message**. Whenever a bidder has bid the item successfully, or 20 seconds has passed, the server will sends a message to all the logged-in clients.
> 6. **Sending private message**. When a transaction is finally confirmed, a notification message will be sent to the client  




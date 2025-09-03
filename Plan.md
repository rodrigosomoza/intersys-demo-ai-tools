# Context
This a multimodule maven project containing two submodules that are rest servers: module 1 and module 2.

# Feature to implement
This is a demo project where the rest server module 1 can receive a request for a financial transaction where a user A can send money to a user B.
Server 1 sends a request to the server 2 as an Object first containing a unique cache-friendly value that identifies the unique transaction request and
another field of name userId which is an array of user ids. The server 2 returns an array of a structure containing the users with their info, especially 
containing the balance of their main account. Server 1 receives the response, and check that the transaction id are identical, then check if the user A
has enough money to send to the user B. The Server 1, if everything is good for the balance, returns a state (succeed or failed) with the new balance after
the reduction. The server 1 also update the user balance account in the database.
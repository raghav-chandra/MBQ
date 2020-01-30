# MBQ
An intelligent queue to provide messages based on how you want it and configured your push, Parallel or Sequential.
* Sequential Processing : Provide SequenceKey and all the messages that has same SequenceKey will be processed in sequantial manner based on FIFO order.
* Parallel Processing : Don'n provide sequenceKey and you are good to consume all messages in parallel based on the FIFO order. 
* Queue can be configured to run in a Single JVM with Producer Consumer running as multiple Threads or as a Centralized Service where multiple JVM can connect to the Queue and Consume/Publish messages. Centralized queue is a language independent Queue, Just needs a client to connect to the Queue Server. 
* You can schedule messages in the queue. MBQ will provide message to the consumers at scheduled time.
* Don't worry if MBQ Server crashes, your items are safe in a Data store. There are various implementation of MBQ available to choose from, In Memory, RDBMS, Hazecast, MongoDB etc.


##How MBQ Works : `

XYZ
(Item #2 - SeqKey A), (Item #1 - SeqKey A)=>Consumer 1
                     (Item #3 - No SeqKey)=>Consumer 2
                     (Item #4 - No SeqKey)=>Consumer 3
                      (Item #5 - SeqKey K)=>Consumer 4
 
 ABC
 (Item #2 - SeqKey A), (Item #1 - SeqKey A)=>Consumer 5

* 1. You can have Dynamic no of logical queues within MBQ. Queue XYZ and ABC shows 2 logical queues within MBQ. 
* 2. Lets look at Queue XYZ and see how Items are being processed 
  * a) Item #1 and #2 are stamped with SequenceKey A  -> Only one consumer can consume #1, #2 and in the same order #1 and then #2
  * b) Item #3 and #4 Doesn't has any Seq Key -> SInce there's no Seq Key, #3, #4 can be coneumed by either 1 consumer or multiple consumer at the same time. 
  * c) Item #5, #6, #7 has SequenceKey K -> Only one consumer can consume #5 and in the order
  
* 3. In case of SequenceKey, Messages will be processed in Order and if any message fails, none of teh consumer will pick subsequent message until Failure is resolved or Failed message is moved out of the queue. 

URL Form Encoding Parser
=================

An implementation of a thread safe URL Form Encoded stream parser.
The parser is built to be tolerant of Stream lengths and max length requirements such as in a Web server environment. 

When parsing for a Front Controller it is often necessary and prudent to cut off the stream at some arbitrary length
in order to protect the Application. This interface is prepared for this outcome.
 
The mechanism is implemented using a state pattern and is thread safe.



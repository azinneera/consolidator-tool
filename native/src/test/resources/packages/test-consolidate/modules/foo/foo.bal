import ballerina/io;

configurable string name = ?;

public function foo() {
    io:println("Hello, " + name + "!");
}
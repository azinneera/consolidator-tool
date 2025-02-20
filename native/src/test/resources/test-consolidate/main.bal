import ballerina/io;

configurable boolean isAdmin = ?;
configurable byte age = ?;
configurable int port = ?;
configurable float height = ?;
configurable decimal salary = ?;
configurable string name = "John";
configurable xml book = ?;
configurable boolean[] switches = ?;
configurable byte[] ages = ?;
configurable int[] ports = ?;
configurable float[] rates = ?;
configurable decimal[] prices = ?;
configurable string[] colors = ?;
// configurable [string, int, string[], map, int... ] student = ?;
configurable map <string> person = ?;
configurable map <byte> personByte = ?;
configurable map <int> personInt = ?;
configurable map <boolean> personBool = ?;
configurable map <float> personFloat = ?;
configurable map <decimal> personDecimal = ?;

configurable map <string>[] people = ?;

type Person record {
  string name;
  int age;
};
configurable Person person1 = ?;
type Food record {
  string name;
  int cal;
};
type Diet record {
  Food food;
  int age;
};
configurable Diet input = ?;
configurable Person[] people2 = ?;
configurable table <map<string>> users = ?;
configurable table <map<string>>[] userTeams = ?;

enum Country {
  LK = "Sri Lanka" ,
  US = "United States"
}
configurable Country country = ?;

configurable int|string code = ?;
configurable anydata data = ?;
configurable json payload = ?;

public function main() {
    io:println("Hello, " + name + "!");
}

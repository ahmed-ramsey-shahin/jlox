# Lox is a dynamically typed language (just for training purposes), these are some examples :)
## Example on variables
```
var x = 12;
var y = x + 2;
```

## Examples on loops

```
for(var i = 0; i < 10; i = i + 1) {
  print i; // print statement that prints a line.
  printF(i); // print function that prints a line without termination.
  printFLine(i); // print function that prints a line with termination, and similar to print statement.
}
```

```
var i = 0;
while(i < 10) {
  print clock(); // prints the time passed in seconds since the begging of the program
  i = i + 1;
}
```

## Examples on function

```
fun power(a, b) {
  var result = 1;
  for(var i = 1; i <= b; i = i + 1) {
    result = result * a;
  }
  return result;
}
print power(2, 3);
```

## Example on classes and inheritance

```
class A {
  init(x) { // constructor
    this.x = x;
  }
  z() {
    return this.x;
  }
}

class B < A { // class B inherits A
  init(x, y) { // constructor which calls the parent class constructor
    super.init(x);
    this.y = y;
  }
  z() {
    return super.z() + this.y;
  }
}
var a = A(3);
var b = B(4, 5);
print a.z();
print b.z();
```

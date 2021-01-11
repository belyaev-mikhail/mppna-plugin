#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "mylib.h"

int pass_string(char* str) {
   return strlen(str);
}

char* return_string() {
    return "MPPNA";
}

int copy_string(char* str, int size) {
  *str++ = 'M';
  *str++ = 'P';
  *str++ = 'P';
  *str++ = 'N';
  *str++ = 'A';
  *str++ = 0;
  return 0;
}

unsigned char return_unsigned_char() {
    return 1 << 7;
}

unsigned short return_unsigned_short() {
    return 1 << 15;
}

unsigned int return_unsigned_int() {
    return 1 << 31;
}

unsigned long long return_unsigned_long_long() {
    return (unsigned long long)1 << 63;
}

int* return_pointer_to_int() {
    int* a = (int*) malloc(3 * sizeof(int));
    a[0] = 0;
    a[1] = 1;
    a[2] = 2;
    return a;
}

int pass_pointer_to_int(int* ptr) {
    int sum = ptr[0] + ptr[1] + ptr[2];
    free(ptr);
    return sum;
}

#ifndef mylib_h
#define mylib_h

int pass_string(char* str);
char* return_string();
int copy_string(char* str, int size);

unsigned char return_unsigned_char();
unsigned short return_unsigned_short();
unsigned int return_unsigned_int();
unsigned long long return_unsigned_long_long();

int* return_pointer_to_int();
int pass_pointer_to_int(int* ptr);

#endif

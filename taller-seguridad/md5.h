#ifndef MD5_H
#define MD5_H

#ifndef uint32
typedef unsigned int uint32;
#endif

struct MD5Context
{
  uint32 buf[4];
  uint32 bits[2];
  unsigned char in[64];
};

extern void MD5Init (struct MD5Context *ctx);
extern void MD5Update (struct MD5Context *ctx, unsigned char *buf,
    unsigned len);
extern void MD5Final (unsigned char digest[16], struct MD5Context *ctx);
extern void MD5Transform (uint32 buf[4], uint32 in[16]);

extern void md5_buf_to_hex(void* buf, unsigned int len, char* digest);

/*
 * This is needed to make RSAREF happy on some MS-DOS compilers.
 */
typedef struct MD5Context MD5_CTX;

#endif /* !MD5_H */

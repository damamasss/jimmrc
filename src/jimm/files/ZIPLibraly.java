/*
package jimm.files;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class m extends OutputStream
{

    public m(java.io.OutputStream outputstream)
    {
        a = outputstream;
    }

    public void close()
    {
        flush();
        a.close();
    }

    public void flush()
    {
        a.flush();
    }

    public void write(int i)
    {
        a.write(i);
    }

    public void write(byte abyte0[])
    {
        write(abyte0, 0, abyte0.length);
    }

    public void write(byte abyte0[], int i, int j)
    {
        for(int k = 0; k < j; k++)
            write(((int) (abyte0[i + k])));

    }

    protected OutputStream a;
}


public class cm extends m
{

    private void b()
    {
        int i;
        for(; !a_af_fld.b() && (i = a_af_fld.a(a_byte_array1d_fld, 0, a_byte_array1d_fld.length)) > 0; a_java_io_OutputStream_fld.write(a_byte_array1d_fld, 0, i));
        if(!a_af_fld.b())
            throw new IOException("Can't deflate all input?");
        else
            return;
    }

    public cm(java.io.OutputStream outputstream, af af1)
    {
        this(outputstream, af1, 4096);
    }

    private cm(java.io.OutputStream outputstream, af af1, int i)
    {
        super(outputstream);
        a_byte_array1d_fld = new byte[4096];
        a_af_fld = af1;
    }

    public void flush()
    {
        a_af_fld.b();
        b();
        a.flush();
    }

    public void a()
    {
        a_af_fld.c();
        int i;
        for(; !a_af_fld.a() && (i = a_af_fld.a(a_byte_array1d_fld, 0, a_byte_array1d_fld.length)) > 0; a.write(a_byte_array1d_fld, 0, i));
        if(!a_af_fld.a())
        {
            throw new IOException("Can't deflate all input?");
        } else
        {
            a.flush();
            return;
        }
    }

    public void close()
    {
        a();
        a.close();
    }

    public void write(int i)
    {
        byte abyte0[];
        (abyte0 = new byte[1])[0] = (byte)i;
        write(abyte0, 0, 1);
    }

    public void write(byte abyte0[], int i, int j)
    {
        a_af_fld.a(abyte0, i, j);
        b();
    }

    private byte a_byte_array1d_fld[];
    protected af a_af_fld;
}


public final class cw extends cm // pack zip
{

    public cw(java.io.OutputStream outputstream)
    {
        super(outputstream, new af(-1, true));
        a_java_util_Vector_fld = new Vector();
        a_bc_fld = new bc();
        a_v_fld = null;
        c = 0;
        a_byte_array1d_fld = new byte[0];
        d = 8;
    }

    private void a(int i)
    {
        a.write(i & 0xff);
        a.write(i >> 8 & 0xff);
    }

    private void b(int i)
    {
        a(i);
        a(i >> 16);
    }

    public final void a(v v1)
    {
        if(a_java_util_Vector_fld == null)
            throw new bo("ZipOutputStream was finished");
        int i = v1.b();
        int j = 0;
        if(i == -1)
            i = d;
        if(i == 0)
        {
            if(v1.c() >= 0L)
            {
                if(v1.b() < 0L)
                    v1.b(v1.c());
                else
                if(v1.b() != v1.c())
                    throw new bo("Method STORED, but compressed size != size");
            } else
            {
                v1.c(v1.b());
            }
            if(v1.b() < 0L)
                throw new bo("Method STORED, but size not set");
            if(v1.d() < 0L)
                throw new bo("Method STORED, but crc not set");
        } else
        if(i == 8 && (v1.c() < 0L || v1.b() < 0L || v1.d() < 0L))
            j = 8;
        if(a_v_fld != null)
            b();
        if(v1.a() < 0L)
            v1.a(java.lang.System.currentTimeMillis());
        v1.a = j;
        v1.b = c;
        v1.b(i);
        a_int_fld = i;
        long l = 0x4034b50L;
        cw cw1 = this;
        b((int)l);
        a(i != 0 ? 20 : 10);
        a(j);
        a(i);
        b(v1.a());
        if((j & 8) == 0)
        {
            b((int)v1.d());
            b((int)v1.c());
            b((int)v1.b());
        } else
        {
            b(0);
            b(0);
            b(0);
        }
        byte abyte0[];
        try
        {
            abyte0 = v1.a().getBytes("UTF-8");
        }
        catch(java.io.UnsupportedEncodingException unsupportedencodingexception)
        {
            throw new ea(((java.lang.Object) (unsupportedencodingexception)));
        }
        if(abyte0.length > 65535)
            throw new bo("Name too long.");
        byte abyte1[];
        if((abyte1 = v1.a()) == null)
            abyte1 = new byte[0];
        a(abyte0.length);
        a(abyte1.length);
        a.write(abyte0);
        a.write(abyte1);
        c += 30 + abyte0.length + abyte1.length;
        a_v_fld = v1;
        a_bc_fld.a();
        if(i == 8)
            a_af_fld.a();
        b = 0;
    }

    private void b()
    {
        if(a_v_fld == null)
            throw new bo("No open entry");
        if(a_int_fld == 8)
            super.a();
        int i = a_int_fld != 8 ? b : a_af_fld.a();
        if(a_v_fld.b() < 0L)
            a_v_fld.b(b);
        else
        if(a_v_fld.b() != (long)b)
            throw new bo("size was " + b + ", but I expected " + a_v_fld.b());
        if(a_v_fld.c() < 0L)
            a_v_fld.c(i);
        else
        if(a_v_fld.c() != (long)i)
            throw new bo("compressed size was " + i + ", but I expected " + a_v_fld.b());
        if(a_v_fld.d() < 0L)
            a_v_fld.d(a_bc_fld.a());
        else
        if(a_v_fld.d() != a_bc_fld.a())
            throw new bo("crc was " + a_bc_fld.a() + ", but I expected " + a_v_fld.d());
        c += i;
        if(a_int_fld == 8 && (a_v_fld.a & 8) != 0)
        {
            long l = 0x8074b50L;
            cw cw1 = this;
            b((int)l);
            b((int)a_v_fld.d());
            b((int)a_v_fld.c());
            b((int)a_v_fld.b());
            c += 16;
        }
        a_java_util_Vector_fld.addElement(((java.lang.Object) (a_v_fld)));
        a_v_fld = null;
    }

    public final void write(byte abyte0[], int i, int j)
    {
        if(a_v_fld == null)
            throw new bo("No open entry.");
        switch(a_int_fld)
        {
        case 8: // '\b'
            super.write(abyte0, i, j);
            break;

        case 0: // '\0'
            a.write(abyte0, i, j);
            break;
        }
        a_bc_fld.a(abyte0, i, j);
        b += j;
    }

    public final void a()
    {
        if(a_java_util_Vector_fld == null)
            return;
        if(a_v_fld != null)
            b();
        int i = 0;
        int j = 0;
        for(java.util.Enumeration enumeration = a_java_util_Vector_fld.elements(); enumeration.hasMoreElements();)
        {
            v v1;
            int k = (v1 = (v)enumeration.nextElement()).b();
            long l = 0x2014b50L;
            cw cw1 = this;
            b((int)l);
            a(k != 0 ? 20 : 10);
            a(k != 0 ? 20 : 10);
            a(v1.a);
            a(k);
            b(v1.a());
            b((int)v1.d());
            b((int)v1.c());
            b((int)v1.b());
            byte abyte0[];
            try
            {
                abyte0 = v1.a().getBytes("UTF-8");
            }
            catch(java.io.UnsupportedEncodingException unsupportedencodingexception)
            {
                throw new ea(((java.lang.Object) (unsupportedencodingexception)));
            }
            if(abyte0.length > 65535)
                throw new bo("Name too long.");
            byte abyte1[];
            if((abyte1 = v1.a()) == null)
                abyte1 = new byte[0];
            java.lang.Object obj = ((java.lang.Object) (v1));
            obj = null;
            byte abyte2[];
            try
            {
                abyte2 = obj == null ? new byte[0] : ((java.lang.String) (obj)).getBytes("UTF-8");
            }
            // Misplaced declaration of an exception variable
            catch(int i)
            {
                throw new ea(((java.lang.Object) (i)));
            }
            if(abyte2.length > 65535)
                throw new bo("Comment too long.");
            a(abyte0.length);
            a(abyte1.length);
            a(abyte2.length);
            a(0);
            a(0);
            b(0);
            b(v1.b);
            a.write(abyte0);
            a.write(abyte1);
            a.write(abyte2);
            i++;
            j += 46 + abyte0.length + abyte1.length + abyte2.length;
        }

        long l1 = 0x6054b50L;
        cw cw2 = this;
        b((int)l1);
        a(0);
        a(0);
        a(i);
        a(i);
        b(j);
        b(c);
        a(a_byte_array1d_fld.length);
        a.write(a_byte_array1d_fld);
        a.flush();
        a_java_util_Vector_fld = null;
    }

    private java.util.Vector a_java_util_Vector_fld;
    private bc a_bc_fld;
    private v a_v_fld;
    private int a_int_fld;
    private int b;
    private int c;
    private byte a_byte_array1d_fld[];
    private int d;
}

public class cs extends java.io.InputStream
{

    protected cs(java.io.InputStream inputstream)
    {
        input = inputstream;
    }

    public void mark(int i)
    {
        input.mark(i);
    }

    public boolean markSupported()
    {
        return input.markSupported();
    }

    public void reset()
    {
        input.reset();
    }

    public int available()
    {
        return input.available();
    }

    public long skip(long l)
    {
        return input.skip(l);
    }

    public int read()
    {
        return input.read();
    }

    public int read(byte abyte0[])
    {
        return read(abyte0, 0, abyte0.length);
    }

    public int read(byte abyte0[], int i, int j)
    {
        return input.read(abyte0, i, j);
    }

    public void close()
    {
        input.close();
    }

    protected java.io.InputStream input;
}

public class aq extends cs
{

    public aq(java.io.InputStream inputstream, cr cr1)
    {
        this(inputstream, cr1, 4096);
    }

    private aq(java.io.InputStream inputstream, cr cr1, int i)
    {
        super(inputstream);
        b = new byte[1];
        if(inputstream == null)
            throw new NullPointerException("in may not be null");
        if(cr1 == null)
        {
            throw new NullPointerException("inf may not be null");
        } else
        {
            a_cr_fld = cr1;
            a_byte_array1d_fld = new byte[4096];
            return;
        }
    }

    public int available()
    {
        if(a_cr_fld == null)
            throw new IOException("stream closed");
        return !a_cr_fld.a() ? 1 : 0;
    }

    public synchronized void close()
    {
        if(input != null)
            input.close();
        input = null;
    }

    public int read()
    {
        int i;
        if((i = read(b, 0, 1)) > 0)
            return b[0] & 0xff;
        else
            return -1;
    }

    public int read(byte abyte0[], int i, int j)
    {
        if(a_cr_fld == null)
            throw new IOException("stream closed");
        if(j == 0)
            return 0;
        do
        {
            int k;
            try
            {
                k = a_cr_fld.a(abyte0, i, j);
            }
            // Misplaced declaration of an exception variable
            catch(byte abyte0[])
            {
                throw new bo(((cn) (abyte0)).getMessage());
            }
            if(k > 0)
                return k;
            if(a_cr_fld.b() | a_cr_fld.a())
                return -1;
            if(a_cr_fld.c())
            {
                aq aq1;
                if((aq1 = this).input == null)
                    throw new bo("InflaterInputStream is closed");
                aq1.a_int_fld = aq1.input.read(aq1.a_byte_array1d_fld, 0, aq1.a_byte_array1d_fld.length);
                if(aq1.a_int_fld < 0)
                    throw new bo("Deflated stream ends early.");
                aq1.a_cr_fld.a(aq1.a_byte_array1d_fld, 0, aq1.a_int_fld);
            } else
            {
                throw new cq("Don't know what to do");
            }
        } while(true);
    }

    public long skip(long l)
    {
        if(a_cr_fld == null)
            throw new IOException("stream closed");
        if(l < 0L)
            throw new IllegalArgumentException();
        if(l == 0L)
            return 0L;
        int i;
        byte abyte0[] = new byte[i = (int)java.lang.Math.min(l, 2048L)];
        long l1 = 0L;
        for(; l > 0L && (i = read(abyte0, 0, i)) > 0; i = (int)java.lang.Math.min(l, 2048L))
        {
            l -= i;
            l1 += i;
        }

        return l1;
    }

    public boolean markSupported()
    {
        return false;
    }

    public void mark(int i)
    {
    }

    public void reset()
    {
        throw new IOException("reset not supported");
    }

    protected cr a_cr_fld;
    protected byte a_byte_array1d_fld[];
    protected int a_int_fld;
    private byte b[];
}


public final class dw extends aq // unpack zip
{

    public dw(java.io.InputStream inputstream)
    {
        super(inputstream, new cr(true));
        a_bc_fld = new bc();
        a_v_fld = null;
    }

    private void a()
    {
        f = a_int_fld = input.read(a_byte_array1d_fld, 0, a_byte_array1d_fld.length);
    }

    private int a(byte abyte0[], int i, int j)
    {
        if(f <= 0)
        {
            a();
            if(f <= 0)
                return -1;
        }
        if(j > f)
            j = f;
        java.lang.System.arraycopy(((java.lang.Object) (a_byte_array1d_fld)), a_int_fld - f, ((java.lang.Object) (abyte0)), i, j);
        f -= j;
        return j;
    }

    private void a(byte abyte0[])
    {
        int i = 0;
        int k;
        for(int j = abyte0.length; j > 0; j -= k)
        {
            if((k = a(abyte0, i, j)) == -1)
                throw new EOFException();
            i += k;
        }

    }

    private int a()
    {
        if(f <= 0)
        {
            a();
            if(f <= 0)
                throw new bo("EOF in header");
        }
        return a_byte_array1d_fld[a_int_fld - f--] & 0xff;
    }

    private int b()
    {
        return a() | a() << 8;
    }

    private int c()
    {
        return b() | b() << 16;
    }

    public final v a()
    {
        if(a_bc_fld == null)
            throw new IOException("Stream closed.");
        if(a_v_fld != null)
            b();
        int i;
        if((long)(i = c()) == 0x2014b50L)
        {
            close();
            return null;
        }
        if((long)i != 0x4034b50L)
            throw new bo("Wrong Local header signature: " + java.lang.Integer.toHexString(i));
        b();
        e = b();
        d = b();
        i = c();
        int j = c();
        b = c();
        c = c();
        int k = b();
        int l = b();
        if(d == 0 && b != c)
            throw new bo("Stored, but compressed != uncompressed");
        byte abyte1[] = new byte[k];
        a(abyte1);
        java.lang.String s;
        try
        {
            s = new String(abyte1, "UTF-8");
        }
        // Misplaced declaration of an exception variable
        catch(int i)
        {
            throw new ea(((java.lang.Object) (i)));
        }
        a_v_fld = new v(s = s);
        a_boolean_fld = false;
        a_v_fld.b(d);
        if((e & 8) == 0)
        {
            a_v_fld.d((long)j & 0xffffffffL);
            a_v_fld.b((long)c & 0xffffffffL);
            a_v_fld.c((long)b & 0xffffffffL);
        }
        a_v_fld.a(i);
        if(l > 0)
        {
            byte abyte0[] = new byte[l];
            a(abyte0);
            a_v_fld.a(abyte0);
        }
        if(d == 8 && f > 0)
        {
            java.lang.System.arraycopy(((java.lang.Object) (a_byte_array1d_fld)), a_int_fld - f, ((java.lang.Object) (a_byte_array1d_fld)), 0, f);
            a_int_fld = f;
            f = 0;
            a_cr_fld.a(a_byte_array1d_fld, 0, a_int_fld);
        }
        return a_v_fld;
    }

    private void b()
    {
        if(a_bc_fld == null)
            throw new IOException("Stream closed.");
        if(a_v_fld == null)
            return;
        if(d == 8)
        {
            if((e & 8) != 0)
            {
                for(byte abyte0[] = new byte[2048]; read(abyte0) > 0;);
                return;
            }
            b -= a_cr_fld.b();
            f = a_cr_fld.a();
        }
        if(f > b && b >= 0)
        {
            f -= b;
        } else
        {
            b -= f;
            f = 0;
            long l;
            for(; b != 0; b -= ((int) (l)))
                if((l = input.skip((long)b & 0xffffffffL)) <= 0L)
                    throw new bo("zip archive ends early.");

        }
        c = 0;
        a_bc_fld.a();
        if(d == 8)
            a_cr_fld.a();
        a_v_fld = null;
        a_boolean_fld = true;
    }

    public final int available()
    {
        return !a_boolean_fld ? 1 : 0;
    }

    public final int read()
    {
        byte abyte0[] = new byte[1];
        if(read(abyte0, 0, 1) <= 0)
            return -1;
        else
            return abyte0[0] & 0xff;
    }

    public final int read(byte abyte0[], int i, int j)
    {
        if(j == 0)
            return 0;
        if(a_bc_fld == null)
            throw new IOException("Stream closed.");
        if(a_v_fld == null)
            return -1;
        java.lang.Object obj = 0;
        switch(d)
        {
        case 8: // '\b'
            if((j = super.read(abyte0, i, j)) < 0)
            {
                if(!a_cr_fld.a())
                    throw new bo("Inflater not finished!?");
                f = a_cr_fld.a();
                if((e & 8) != 0)
                {
                    if((long)((dw) (obj = ((java.lang.Object) (this)))).c() != 0x8074b50L)
                        throw new bo("Data descriptor signature not found");
                    ((dw) (obj)).a_v_fld.d((long)((dw) (obj)).c() & 0xffffffffL);
                    obj.b = ((dw) (obj)).c();
                    obj.c = ((dw) (obj)).c();
                    ((dw) (obj)).a_v_fld.b((long)((dw) (obj)).c & 0xffffffffL);
                    ((dw) (obj)).a_v_fld.c((long)((dw) (obj)).b & 0xffffffffL);
                }
                if(a_cr_fld.b() != b || a_cr_fld.c() != c)
                    throw new bo("size mismatch: " + b + ";" + c + " <-> " + a_cr_fld.b() + ";" + a_cr_fld.c());
                a_cr_fld.a();
                obj = 1;
            }
            break;

        case 0: // '\0'
            if(j > b && b >= 0)
                j = b;
            if((j = a(abyte0, i, j)) > 0)
            {
                b -= j;
                c -= j;
            }
            if(b == 0)
                obj = 1;
            else
            if(j < 0)
                throw new bo("EOF in stored block");
            break;
        }
        if(j > 0)
            a_bc_fld.a(abyte0, i, j);
        if(obj != 0)
        {
            if((a_bc_fld.a() & 0xffffffffL) != a_v_fld.d())
                throw new bo("CRC mismatch");
            a_bc_fld.a();
            a_v_fld = null;
            a_boolean_fld = true;
        }
        return j;
    }

    public final void close()
    {
        super.close();
        a_bc_fld = null;
        a_v_fld = null;
        a_boolean_fld = true;
    }

    private bc a_bc_fld;
    private v a_v_fld;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private boolean a_boolean_fld;
}

public final class bc
{

    public bc()
    {
        a_int_fld = 0;
    }

    public final long a()
    {
        return (long)a_int_fld & 0xffffffffL;
    }

    public final void a()
    {
        a_int_fld = 0;
    }

    public final void a(byte abyte0[], int i, int j)
    {
        int k;
        for(k = ~a_int_fld; --j >= 0; k = a_int_array1d_static_fld[(k ^ abyte0[i++]) & 0xff] ^ k >>> 8);
        a_int_fld = ~k;
    }

    private int a_int_fld;
    private static int a_int_array1d_static_fld[];

    static
    {
        int ai[] = new int[256];
        for(int i = 0; i < 256; i++)
        {
            int j = i;
            for(int k = 8; --k >= 0;)
                if((j & 1) != 0)
                    j = 0xedb88320 ^ j >>> 1;
                else
                    j >>>= 1;

            ai[i] = j;
        }

        a_int_array1d_static_fld = ai;
    }
}

public final class v
{

    public v(java.lang.String s)
    {
        a_long_fld = -1L;
        b_java_lang_String_fld = null;
        a_byte_fld = -1;
        b_byte_fld = 0;
        a_byte_array1d_fld = null;
        int i;
        if((i = s.length()) > 65535)
        {
            throw new IllegalArgumentException("name length is " + i);
        } else
        {
            a_java_lang_String_fld = s;
            return;
        }
    }

    final void a(int i)
    {
        e = i;
        b_byte_fld |= 0x10;
        b_byte_fld &= 0xf7;
    }

    final int a()
    {
        if((b_byte_fld & 0x10) != 0)
            return e;
        if((b_byte_fld & 8) != 0)
        {
            java.util.Calendar calendar;
            (calendar = java.util.Calendar.getInstance()).setTime(new Date(b_long_fld));
            e = (calendar.get(1) - 1980 & 0x7f) << 25 | calendar.get(2) + 1 << 21 | calendar.get(5) << 16 | calendar.get(11) << 11 | calendar.get(12) << 5 | calendar.get(13) >> 1;
            b_byte_fld |= 0x10;
            return e;
        } else
        {
            return 0;
        }
    }

    public final java.lang.String a()
    {
        return a_java_lang_String_fld;
    }

    public final void a(long l)
    {
        b_long_fld = l;
        b_byte_fld |= 8;
        b_byte_fld &= 0xef;
    }

    public final long a()
    {
        int i;
        int k;
        int i1;
        int k1;
        int l1;
        int i2;
        v v1;
        if(((v1 = this).b_byte_fld & 0x20) == 0)
            if(v1.a_byte_array1d_fld == null)
            {
                v1.b_byte_fld |= 0x20;
            } else
            {
                try
                {
                    int j1;
                    for(int j = 0; j < v1.a_byte_array1d_fld.length; j += j1)
                    {
                        int l = v1.a_byte_array1d_fld[j++] & 0xff | (v1.a_byte_array1d_fld[j++] & 0xff) << 8;
                        j1 = v1.a_byte_array1d_fld[j++] & 0xff | (v1.a_byte_array1d_fld[j++] & 0xff) << 8;
                        if(l == 21589 && ((l = ((int) (v1.a_byte_array1d_fld[j]))) & 1) != 0)
                        {
                            long l2 = v1.a_byte_array1d_fld[j + 1] & 0xff | (v1.a_byte_array1d_fld[j + 2] & 0xff) << 8 | (v1.a_byte_array1d_fld[j + 3] & 0xff) << 16 | (v1.a_byte_array1d_fld[j + 4] & 0xff) << 24;
                            v1.a(l2 * 1000L);
                        }
                    }

                }
                catch(java.lang.ArrayIndexOutOfBoundsException _ex) { }
                v1.b_byte_fld |= 0x20;
            }
        if((b_byte_fld & 8) != 0)
            return b_long_fld;
        if((b_byte_fld & 0x10) == 0)
            break MISSING_BLOCK_LABEL_421;
        i = 2 * (e & 0x1f);
        k = e >> 5 & 0x3f;
        i1 = e >> 11 & 0x1f;
        k1 = e >> 16 & 0x1f;
        l1 = (e >> 21 & 0xf) - 1;
        i2 = (e >> 25 & 0x7f) + 1980;
        java.util.Calendar calendar;
        (calendar = java.util.Calendar.getInstance()).set(1, i2);
        calendar.set(2, l1);
        calendar.set(5, k1);
        calendar.set(11, i1);
        calendar.set(12, k);
        calendar.set(13, i);
        b_long_fld = calendar.getTime().getTime();
        b_byte_fld |= 8;
        return b_long_fld;
        JVM INSTR pop ;
        b_byte_fld &= 0xf7;
        return -1L;
        return -1L;
    }

    public final void b(long l)
    {
        if((l & 0xffffffff00000000L) != 0L)
        {
            throw new IllegalArgumentException();
        } else
        {
            c = (int)l;
            b_byte_fld |= 1;
            return;
        }
    }

    public final long b()
    {
        if((b_byte_fld & 1) != 0)
            return (long)c & 0xffffffffL;
        else
            return -1L;
    }

    public final void c(long l)
    {
        a_long_fld = l;
    }

    public final long c()
    {
        return a_long_fld;
    }

    public final void d(long l)
    {
        if((l & 0xffffffff00000000L) != 0L)
        {
            throw new IllegalArgumentException();
        } else
        {
            d = (int)l;
            b_byte_fld |= 4;
            return;
        }
    }

    public final long d()
    {
        if((b_byte_fld & 4) != 0)
            return (long)d & 0xffffffffL;
        else
            return -1L;
    }

    public final void b(int i)
    {
        if(i != 0 && i != 8)
        {
            throw new IllegalArgumentException();
        } else
        {
            a_byte_fld = (byte)i;
            return;
        }
    }

    public final int b()
    {
        return ((int) (a_byte_fld));
    }

    public final void a(byte abyte0[])
    {
        if(abyte0 == null)
        {
            a_byte_array1d_fld = null;
            return;
        }
        if(abyte0.length > 65535)
        {
            throw new IllegalArgumentException();
        } else
        {
            a_byte_array1d_fld = abyte0;
            return;
        }
    }

    public final byte[] a()
    {
        return a_byte_array1d_fld;
    }

    public final boolean a()
    {
        int i;
        return (i = a_java_lang_String_fld.length()) > 0 && a_java_lang_String_fld.charAt(i - 1) == '/';
    }

    public final java.lang.String toString()
    {
        return a_java_lang_String_fld;
    }

    public final int hashCode()
    {
        return a_java_lang_String_fld.hashCode();
    }

    private final java.lang.String a_java_lang_String_fld;
    private int c;
    private long a_long_fld;
    private int d;
    private java.lang.String b_java_lang_String_fld;
    private byte a_byte_fld;
    private byte b_byte_fld;
    private int e;
    private long b_long_fld;
    private byte a_byte_array1d_fld[];
    int a_int_fld;
    int b_int_fld;
}

public final class af
{

    public af()
    {
        this(-1, false);
    }

    public af(int i, boolean flag)
    {
        a_ca_fld = new ca();
        a_u_fld = new u(a_ca_fld);
        a_boolean_fld = flag;
        flag = false;
        i = ((int) (this));
        a_u_fld.a(0);
        flag = 6;
        i = ((int) (this));
        if(flag == -1)
        {
            flag = 6;
            break MISSING_BLOCK_LABEL_81;
        }
        flag;
        JVM INSTR iflt 73;
           goto _L1 _L2
_L1:
        break MISSING_BLOCK_LABEL_67;
_L2:
        break MISSING_BLOCK_LABEL_73;
        if(flag <= 9)
            break MISSING_BLOCK_LABEL_81;
        throw new IllegalArgumentException();
        if(((af) (i)).a_int_fld != flag)
        {
            i.a_int_fld = ((int) (flag));
            ((af) (i)).a_u_fld.b(((int) (flag)));
        }
        a();
        return;
    }

    public final void a()
    {
        b = a_boolean_fld ? 16 : 0;
        a_long_fld = 0L;
        ca ca1;
        (ca1 = a_ca_fld).a = ca1.b = ca1.c = 0;
        a_u_fld.a();
    }

    public final int a()
    {
        return (int)a_long_fld;
    }

    final void b()
    {
        b |= 4;
    }

    public final void c()
    {
        b |= 0xc;
    }

    public final boolean a()
    {
        return b == 30 && a_ca_fld.a();
    }

    public final boolean b()
    {
        return a_u_fld.a();
    }

    public final void a(byte abyte0[], int i, int k)
    {
        if((b & 8) != 0)
        {
            throw new IllegalStateException("finish()/end() already called");
        } else
        {
            a_u_fld.a(abyte0, i, k);
            return;
        }
    }

    public final int a(byte abyte0[], int i, int k)
    {
        int l = k;
        if(b == 127)
            throw new IllegalStateException("Deflater closed");
        if(b < 16)
        {
            int i1;
            if((i1 = a_int_fld - 1 >> 1) < 0 || i1 > 3)
                i1 = 3;
            i1 = 0x7800 | i1 << 6;
            if((b & 1) != 0)
                i1 |= 0x20;
            i1 += 31 - i1 % 31;
            a_ca_fld.b(i1);
            if((b & 1) != 0)
            {
                int j1 = a_u_fld.a();
                a_u_fld.b();
                a_ca_fld.b(j1 >> 16);
                a_ca_fld.b(j1 & 0xffff);
            }
            b = 0x10 | b & 0xc;
        }
        do
        {
            int k1 = a_ca_fld.a(abyte0, i, k);
            i += k1;
            a_long_fld += k1;
            if((k -= k1) == 0 || b == 30)
                break;
            if(!a_u_fld.a((b & 4) != 0, (b & 8) != 0))
            {
                if(b == 16)
                    return l - k;
                if(b == 20)
                {
                    if(a_int_fld != 0)
                    {
                        ca ca1;
                        for(int l1 = 8 + (-((j) (ca1 = a_ca_fld)).c & 7); l1 > 0; l1 -= 10)
                            a_ca_fld.a(2, 10);

                    }
                    b = 16;
                } else
                if(b == 28)
                {
                    a_ca_fld.a();
                    if(!a_boolean_fld)
                    {
                        int i2 = a_u_fld.a();
                        a_ca_fld.b(i2 >> 16);
                        a_ca_fld.b(i2 & 0xffff);
                    }
                    b = 30;
                }
            }
        } while(true);
        return l - k;
    }

    private int a_int_fld;
    private boolean a_boolean_fld;
    private int b;
    private long a_long_fld;
    private ca a_ca_fld;
    private u a_u_fld;
}

final class ca extends j
{

    public ca()
    {
        super(0x10000);
    }
}

class j
{

    public j()
    {
        this(4096);
    }

    public j(int i)
    {
        a_byte_array1d_fld = new byte[i];
    }

    public final void a(int i)
    {
        a_byte_array1d_fld[b++] = (byte)i;
        a_byte_array1d_fld[b++] = (byte)(i >> 8);
    }

    public final void a(byte abyte0[], int i, int k)
    {
        java.lang.System.arraycopy(((java.lang.Object) (abyte0)), i, ((java.lang.Object) (a_byte_array1d_fld)), b, k);
        b += k;
    }

    public final void a()
    {
        if(c > 0)
        {
            a_byte_array1d_fld[b++] = (byte)d;
            if(c > 8)
                a_byte_array1d_fld[b++] = (byte)(d >>> 8);
        }
        d = 0;
        c = 0;
    }

    public final void a(int i, int k)
    {
        d |= i << c;
        c += k;
        if(c >= 16)
        {
            a_byte_array1d_fld[b++] = (byte)d;
            a_byte_array1d_fld[b++] = (byte)(d >>> 8);
            d >>>= 16;
            c -= 16;
        }
    }

    public final void b(int i)
    {
        a_byte_array1d_fld[b++] = (byte)(i >> 8);
        a_byte_array1d_fld[b++] = (byte)i;
    }

    public final boolean a()
    {
        return b == 0;
    }

    public final int a(byte abyte0[], int i, int k)
    {
        if(c >= 8)
        {
            a_byte_array1d_fld[b++] = (byte)d;
            d >>>= 8;
            c -= 8;
        }
        if(k > b - a_int_fld)
        {
            k = b - a_int_fld;
            java.lang.System.arraycopy(((java.lang.Object) (a_byte_array1d_fld)), a_int_fld, ((java.lang.Object) (abyte0)), i, k);
            a_int_fld = 0;
            b = 0;
        } else
        {
            java.lang.System.arraycopy(((java.lang.Object) (a_byte_array1d_fld)), a_int_fld, ((java.lang.Object) (abyte0)), i, k);
            a_int_fld += k;
        }
        return k;
    }

    private byte a_byte_array1d_fld[];
    int a_int_fld;
    int b;
    private int d;
    int c;
}

*/

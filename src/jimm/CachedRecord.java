package jimm;

// Class to cache one line in messages list

// All fields are public to easy and fast access
public class CachedRecord {
    public String shortText, text, date, from;
    public byte type; // 0 - incoming message, 1 - outgoing message
//#sijapp cond.if target is "MIDP2" | target is "SIEMENS2" | target is "MOTOROLA"#
//    public boolean contains_url;
//#sijapp cond.end#
}
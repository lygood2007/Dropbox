package common;

/**
 * Package: common
 * Interface: ProtocolConstants
 * Description: Defines all of the constants for network protocol
 */
public interface ProtocolConstants {
	final static byte OP_NULL = 0;
	final static byte OP_ADD = 1;
	final static byte OP_DEL = 2;
	final static byte OP_MOD = 3;
	final static int PACK_BEGIN = 0x0fff;
}

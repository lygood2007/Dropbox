package common;

/**
 * Package: common
 * Interface: ProtocolConstants
 * Description: Defines all of the constants for network protocol
 */
public interface ProtocolConstants {
	final static byte OP_NULL = 0x00;
	final static byte OP_ADD = 0x01;
	final static byte OP_DEL = 0x02;
	final static byte OP_MOD = 0x03;
	final static int PACK_DATA_HEAD = 0x00000fff;
	final static int PACK_QUERY_HEAD = 0x000000ff;
	final static int PACK_NULL_HEAD = 0x0000000f;
	final static int PACK_INVALID_HEAD = 0x00000000;
	final static int PACK_BEGIN = 0x0000ffff;

	/* Protocol header between master node and file server */
	final static String PACK_STR_CONFIRM_HEAD = "CFM";
	final static String PACK_STR_HEARTBEAT_HEAD = "HBT";
	final static String PACK_STR_REQUEST_FS_HEAD = "RFS";
	final static String PACK_STR_CLOSE_HEAD = "CLS";
	final static String PACK_STR_REQUEST_MS_HEAD = "RMS";
	final static String PACK_STR_SET_PRIO_HEAD = "SPR";
	final static String PACK_STR_ID_HEAD = "IDF";
	final static String PACK_STR_USR_HEAD = "USR";
}

package com.ibm.ivr.eus.websv;

import com.ibm.ivr.eus.websv.Socket_Connection;

/*
 * Test package for vru_automate()
 * @usage	In RSA, select this file run as->Java Application
 */
public class Test_websv {

	public static void test_bad_hostname() {
		String hostname = "localhost";
		int port = 22228;
		String strRequestNA = "A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=";
		// String strRequestCA = "A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=";
		String serial = "xxxxxx";
		String ccode = "xxx";
		String strRequest = strRequestNA + serial + ",P=" + ccode;
		String strResponse = null;
		int responseTimeout = 60; // seconds
		Socket_Connection sc = new Socket_Connection();

		System.out.println("test_bad_hostname(): vru_automate(\"" + hostname
				+ "\"," + port + ",\"" + strRequest + "\"," + responseTimeout
				+ ")");
		strResponse = sc.vru_automate(hostname, port, strRequest,
				responseTimeout, true, "Test_websv");
		System.out.println("test_bad_hostname(): strResponse=:" + strResponse
				+ ":");
	}

	public static void test_bad_port() {
		String hostname = "RTPDBS05.raleigh.ibm.com";
		int port = 9999;
		String strRequestNA = "A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=";
		// String strRequestCA = "A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=";
		String serial = "653006";
		String ccode = "xxx";
		String strRequest = strRequestNA + serial + ",P=" + ccode;
		String strResponse = null;
		int responseTimeout = 60; // seconds
		Socket_Connection sc = new Socket_Connection();

		System.out.println("test_bad_port(): vru_automate(\"" + hostname
				+ "\"," + port + ",\"" + strRequest + "\"," + responseTimeout
				+ ")");
		strResponse = sc.vru_automate(hostname, port, strRequest,
				responseTimeout, true, "Test_websv");
		System.out
				.println("test_bad_port(): strResponse=:" + strResponse + ":");
	}

	public static void test_rsa_pw_reset() {
		String hostname = "RTPDBS05.raleigh.ibm.com";
		int port = 22228;
		String strRequestNA = "A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=";
		// String strRequestCA = "A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=";
		String serial = "653006";
		String ccode = "xxx";
		String strRequest = strRequestNA + serial + ",P=" + ccode;
		String strResponse = null;
		int responseTimeout = 60; // seconds
		Socket_Connection sc = new Socket_Connection();

		System.out.println("test_rsa_pw_reset(): vru_automate(\"" + hostname
				+ "\"," + port + ",\"" + strRequest + "\"," + responseTimeout
				+ ")");
		strResponse = sc.vru_automate(hostname, port, strRequest,
				responseTimeout, true, "Test_websv");
		System.out.println("test_rsa_pw_reset(): strResponse=:" + strResponse
				+ ":");
	}

	public static void test_host_pw_reset() {
		String hostname = "RTPDBS05.raleigh.ibm.com";
		int port = 22223;
		String strRequestNA = "A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=";
		// String strRequestCA = "A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=";
		String serial = "653006";
		String ccode = "xxx";
		String strRequest = strRequestNA + serial + ",P=" + ccode;
		String strResponse = null;
		int responseTimeout = 60; // seconds
		Socket_Connection sc = new Socket_Connection();

		System.out.println("test_host_pw_reset(): vru_automate(\"" + hostname
				+ "\"," + port + ",\"" + strRequest + "\"," + responseTimeout
				+ ")");
		strResponse = sc.vru_automate(hostname, port, strRequest,
				responseTimeout, true, "Test_websv");
		System.out
				.println("test_host_pw_reset(): strResponse=:" + strResponse + ":");		
	}

	public static void test_ushelp() {
		String hostname = "RTPDBS05.raleigh.ibm.com";
		int port = 22223;
		String strRequestNA = "A=HOSTUSA,M=CTI,D=hostpassword.dll,F=HOSTPASSWORD,S=";
		String serial = "A030BZ";
		// RC=200,CAUSE=(NoIdLocated,HOSTPASSWORD,Application=HOSTUSA,Serial=653006,Country=897)
		// RC=100,CAUSE=(GetBluePagesFailed,HOSTPASSWORD,Application=HOSTUSA,Serial=653006,Country=xxx)
		String ccode = "897";
		String strRequest = strRequestNA + serial + ",P=" + ccode;
		String strResponse = null;
		int responseTimeout = 60; // seconds
		Socket_Connection sc = new Socket_Connection();

		System.out.println("test_ushelp(): vru_automate(\"" + hostname
				+ "\"," + port + ",\"" + strRequest + "\"," + responseTimeout
				+ ")");
		strResponse = sc.vru_automate(hostname, port, strRequest,
				responseTimeout, true, "Test_websv");
		System.out.println("test_ushelp(): strResponse=:" + strResponse
				+ ":");	
	}	
	
	public static void main(String[] args) {
		// test_bad_hostname();
		// test_bad_port();
		// test_rsa_pw_reset();
		// test_host_pw_reset();
		test_ushelp();
	}
}
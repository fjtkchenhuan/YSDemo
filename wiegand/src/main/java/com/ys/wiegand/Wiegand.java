package com.ys.wiegand;

/**
 * Created by Administrator on 2018/1/27.
 */

public class Wiegand {
	public static native int inputOpen();//打开
	public static native void inputClose();//使用完成后关闭
	public static native int inputRead();//读取
	public static native int outputOpen();//打开
	public static native void outputClose();//使用完成后关闭
	public static native int readoutputWrite26();
	public static native int readoutputWrite34();
	public static native int Output26(long value);//传递整型数,将该数按照韦根26协议输出
	public static native int Output34(long value);//传递整型数,将该数按照韦根34协议输出
	static {
		System.loadLibrary("wiegand");
	}
}

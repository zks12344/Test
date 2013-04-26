package com.test;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * ʹ��java�߳�ɨ�������ip�򵥷���
 * 
 * @author Administrator
 * 
 */
public class Ip {

	public static void main(String[] args) {
		System.out.println("��ʼ......");
		Ip ip = new Ip();
		ArrayList<String> list = ip.getLanIPArrayList();
		System.out.println("����ж��ٸ�===>" + list.size());
	}

	public ArrayList<String> getLanIPArrayList() {
		ArrayList<String> arrayIP = null;
		try {
			InitSystem initSystem = null;
			initSystem = new InitSystem();
			Thread thread = new Thread(initSystem);
			thread.start();
			thread.join();
			arrayIP = initSystem.getArrayIPUsed();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return arrayIP;
	}

	private class InitSystem implements Runnable {
		private int firstIP = 2;// ��ѯ�� IP ��ַ�����һλ��ʼ��

		private int lastIP = 255;// ��ѯ�� IP ��ַ�����һλ������

		private volatile ArrayList<Thread> arrayThread;// ���̶߳�

		private final int MAXTHREADNUM = 30; // ���ͬʱ���е����߳�����

		private int threadNumNow;// ��ǰ���ڽ��е����߳�����

		private volatile ArrayList<String> arrayIP;// ��������ѯ���п��ܵ� IP ��ַ�Ľ����

		private volatile ArrayList<String> arrayIPUsed;// ��������ѯ�Ѿ�ʹ�õ� IP ��ַ�Ľ����

		private InitSystem(String ip) {
			System.out.println("IP===>" + ip);
			arrayIP = new ArrayList<String>();
			arrayIPUsed = new ArrayList<String>();
			arrayThread = new ArrayList<Thread>();
			setIPAddressList(ip);
		}

		private InitSystem() throws UnknownHostException {
			this(InetAddress.getLocalHost().getHostAddress());
		}

		private synchronized ArrayList<String> getArrayIPUsed() {
			try {
				System.out.println("getArrayIPUsed:  arrayIP.size===>"
						+ arrayIP.size());
				while (arrayIP.size() > 0) {
					Thread.sleep(300);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return arrayIPUsed;
		}

		private void setIPAddressList(String ip) {
			// ������� ip ��ַ��ѯ�����ڵľ����������п��� IP ��ַ�ļ���
			int lastPointIndex = ip.lastIndexOf('.');
			String stringIPHead = ip.substring(0, ++lastPointIndex);
			System.out.println("stringIPHead===>" + stringIPHead);
			String stringIP = null;
			for (int i = firstIP; i <= lastIP; i++) {
				stringIP = stringIPHead + i;
				arrayIP.add(stringIP);
			}
			System.out.println("���ŵ�����...arrayIP���ܸ�����" + arrayIP.size());
		}

		public void run() {
			synchronized (this) {
				try {
					System.out.println("run()  arrayIP.size()===>"
							+ arrayIP.size());
					System.out
							.println("run()  threadNumNow===>" + threadNumNow);
					System.out.println("arrayThread.size()"
							+ arrayThread.size());
					while (arrayIP.size() > 0) {
						while (threadNumNow >= MAXTHREADNUM) {
							System.out.println("�̳߳���30����ֹ�����...");
							for (Thread thread : arrayThread) {
								if (!thread.getState().equals(
										Thread.State.TERMINATED)) {
									thread.join(5);
								}
								--threadNumNow;
							}
							arrayThread = new ArrayList<Thread>();
						}
						Thread thread = new Thread(new InnerClass(arrayIP
								.remove(0)));
						thread.start();
						threadNumNow++;
						arrayThread.add(thread);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private class InnerClass implements Runnable {
			// �̲߳�ѯһ�� IP �Ƿ��ǿ������ӵ� ������뵽��Ӧ�� IP ����
			private String ip;

			private InnerClass(String ip) {
				System.out.println("InnerClass ip===>" + ip);
				this.ip = ip;
			}

			private boolean isUsedIPAddress(String ip) {
				System.out.println("isUsedIPAddress===>" + ip);
				synchronized (this) {
					System.out.println("����˵�.....");
					// �ж���� IP ��ַ�ڵ�ǰ���������Ƿ��ǿ����ӵ� IP
					Process process = null;
					BufferedReader bufReader = null;
					String bufReadLineString = null;
					try {
						process = Runtime.getRuntime().exec(
								"ping " + ip + " -w 100 -n 1");
						bufReader = new BufferedReader(new InputStreamReader(
								process.getInputStream()));
						for (int i = 0; i < 6 && bufReader != null; i++) {
							bufReader.readLine();
						}
						bufReadLineString = bufReader.readLine();
						// System.out.println("bufReadLineString===>"+bufReadLineString);
						if (bufReadLineString == null) {
							process.destroy();
							return false;
						}
						if (bufReadLineString.indexOf("timed out") > 0
								|| bufReadLineString.length() < 17
								|| bufReadLineString.indexOf("invalid") > 0) {
							process.destroy();
							return false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					process.destroy();
					return true;
				}
			}

			public void run() {
				synchronized (this) {
					if (isUsedIPAddress(ip)) {
						arrayIPUsed.add(ip);
					}
				}
			}
		}
	}
}
package Worker;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CPUMemInfo {
	static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

	public static String printMemUsage(Runtime runtime)
	{
		long total, free, used;
		int kb = 1024;
		String data = "";
		
		total = runtime.totalMemory();
		free = runtime.freeMemory();
		used = total - free;
		data = data + "Total Memory: " + total / kb + "KB\n";
		data = data + "Memory Used: " + used / kb + "KB\n";
		data = data + "Memory Free: " + free / kb + "KB\n";
		data = data + "Percent Used: " + ((double)used/(double)total)*100 + "%\n";
		data = data + "Percent Free: " + ((double)free/(double)total)*100 + "%\n";
		
		return data;
	}

	public static void test()
	{
		for(int i=0;i<10000;i++)
		{
			System.out.print("");	
		}

	}

	public static float printCPUUsage()
	{
		int cpuCount = operatingSystemMXBean.getAvailableProcessors();
		long cpuStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

		long elapsedStartTime = System.nanoTime();
		test();
		long end = System.nanoTime();

		long totalAvailCPUTime = cpuCount * (end-elapsedStartTime);
		long totalUsedCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()-cpuStartTime;
		float per = ((float)totalUsedCPUTime*100);///(float)totalAvailCPUTime;
		return per;
	}

	
	public static String getData() {
		String data = "";
		data = "CPU time is: " + printCPUTime()+ "\n";
        data = data + "Memory usage:\n" +  	printMemUsage(Runtime.getRuntime());	
		return data;
	}

	private static String printCPUTime() {
		String infoString = "";
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		for(Long threadID : threadMXBean.getAllThreadIds()) {
			ThreadInfo info = threadMXBean.getThreadInfo(threadID);
			infoString = infoString + "Thread name: " + info.getThreadName() + "\n";
			infoString = infoString + "Thread State: " + info.getThreadState() + "\n";
			infoString = infoString + String.format("CPU time: %s ns\n", threadMXBean.getThreadCpuTime(threadID));
			
		}
		return infoString;
	}



}



/**
 * @Description:
 * @author: Administrator
 * @date: 2018年1月31日 下午12:13:53
 * @version: 1.0
 */

public class SmsSendAsync
{
    private String prtNo;
    
    public SmsSendAsync(String prtNo)
    {
        this.prtNo = prtNo;
    }
    
    public void send()
    {
        // 新定义线程来处理发送
        new Thread()
        {
            
            public void run()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    
                    e.printStackTrace();
                    
                }
                // 控制台打印日志
                System.out.println("fffffffffffff"+prtNo);
            }
        }.start();
    }
    
    public static void main(String[] args)
    {
        String prtNo = "1001200912310155555";
        // 调用异步发送短信
        SmsSendAsync sendAsync = new SmsSendAsync(prtNo);
        sendAsync.send();
        System.out.println("已经签单");
        
    }
}

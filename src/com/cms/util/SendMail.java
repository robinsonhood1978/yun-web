package com.cms.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.log4j.Logger;

public class SendMail {
	private static Logger logger = Logger.getLogger(SendMail.class); 
	
	public static void send(Map<String,Object> mailserver,final String from,final String passwd,String to,String subject,String htmlbody,String[] atts) {
        Properties properties = System.getProperties();
        
        // Setup mail server
        String smtp_host = mailserver.get("smtp_domain").toString();
		String port = mailserver.get("send_port").toString();
		String ssl_enable = mailserver.get("smtp_ssl_enable").toString().equals("1")?"true":"false";
		String starttls_enable = mailserver.get("smtp_starttls_enable").toString().equals("1")?"true":"false";
		properties.put("mail.smtp.ssl.enable", ssl_enable);
		properties.put("mail.smtp.starttls.enable",starttls_enable);
		properties.put("mail.smtp.host", smtp_host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                //return new PasswordAuthentication("fynco.storage@gmail.com", "fynco321");
            	return new PasswordAuthentication(from, passwd);

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            if (to.indexOf(',') > 0) {
            	message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(to));   
            }
            else {
            	message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }

//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//	
//            

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            //message.setText("This is actual message");
         // Send the actual HTML message.
            //message.setContent(htmlbody,"text/html");
            
         // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart multiPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart bodyPart = new MimeBodyPart();
            // 设置html邮件消息内容
            bodyPart.setContent(htmlbody, "text/html; charset=utf-8");
            multiPart.addBodyPart(bodyPart);

            //添加附件
            if(atts != null){
                for(String attachFile : atts){
                	String[] attArr = attachFile.split("#");
                    bodyPart=new MimeBodyPart();  
                    FileDataSource fds=new FileDataSource(attArr[0]); //得到数据源  
                    bodyPart.setDataHandler(new DataHandler(fds)); //得到附件本身并放入BodyPart  
                    if(attArr.length==1) {
                    	bodyPart.setFileName(MimeUtility.encodeText(fds.getName()));  //得到文件名并编码（防止中文文件名乱码）同样放入BodyPart 
                    }
                    else {
                    	bodyPart.setFileName(MimeUtility.encodeText(attArr[1]));
                    }
                    multiPart.addBodyPart(bodyPart);  
                }
            }

            // 设置邮件消息的主要内容
            message.setContent(multiPart);

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            
            System.out.println("Sent message successfully....");
        } catch (Exception mex) {
            mex.printStackTrace();
        }

	}
	 public static void send( String from, String passwd,String to,String subject,String htmlbody,String[] atts) {
	 	String host = "smtp.taijicoin.nz";
	 	send( host,  from,  passwd, to, subject, htmlbody,atts);
	 }
    // dannel modify
	public static boolean send(String host,final String from,final String passwd,String to,String subject,String htmlbody,String[] atts) {
		boolean result = false;

        Properties properties = hostSetUp(host);

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                //return new PasswordAuthentication("fynco.storage@gmail.com", "fynco321");
            	return new PasswordAuthentication(from, passwd);

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            //message.setText("This is actual message");
         // Send the actual HTML message.
            //message.setContent(htmlbody,"text/html");
            
         // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart multiPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart bodyPart = new MimeBodyPart();
            // 设置html邮件消息内容
            bodyPart.setContent(htmlbody, "text/html; charset=utf-8");
            multiPart.addBodyPart(bodyPart);

            //添加附件
            if(atts!=null && atts.length != 0){
                for(String attachFile : atts){
                    bodyPart=new MimeBodyPart();  
                    FileDataSource fds=new FileDataSource(attachFile); //得到数据源  
                    bodyPart.setDataHandler(new DataHandler(fds)); //得到附件本身并放入BodyPart  
                    bodyPart.setFileName(MimeUtility.encodeText(fds.getName()));  //得到文件名并编码（防止中文文件名乱码）同样放入BodyPart  
                    multiPart.addBodyPart(bodyPart);  
                }
            }

            // 设置邮件消息的主要内容
            message.setContent(multiPart);

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            
            System.out.println("Sent message successfully....");
            result = true;
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return result;

	}
	public static boolean send(String subject,String htmlbody,String[] atts) {
		boolean result = false;
		
		String host = System.getenv().getOrDefault("SMTP_SERVER","smtp.mailgun.org");
    	final String from = System.getenv().getOrDefault("SMTP_USER","postmaster@sandbox9650e85b669c41c18ca5f49c839e1be0.mailgun.org");
    	final String passwd = System.getenv().getOrDefault("SMTP_PASS","476396af3c781dedcf539f9ced87aa4c-e438c741-62b6fac3");
    	String to = System.getenv().getOrDefault("MAIL_TO","robinsonhood1978@gmail.com");
    	
    	logger.warn("from="+from);

        Properties properties = hostSetUp(host);

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                //return new PasswordAuthentication("fynco.storage@gmail.com", "fynco321");
            	return new PasswordAuthentication(from, passwd);

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            //message.setText("This is actual message");
         // Send the actual HTML message.
            //message.setContent(htmlbody,"text/html");
            
         // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart multiPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart bodyPart = new MimeBodyPart();
            // 设置html邮件消息内容
            bodyPart.setContent(htmlbody, "text/html; charset=utf-8");
            multiPart.addBodyPart(bodyPart);

            //添加附件
            if(atts!=null && atts.length != 0){
                for(String attachFile : atts){
                    bodyPart=new MimeBodyPart();  
                    FileDataSource fds=new FileDataSource(attachFile); //得到数据源  
                    bodyPart.setDataHandler(new DataHandler(fds)); //得到附件本身并放入BodyPart  
                    bodyPart.setFileName(MimeUtility.encodeText(fds.getName()));  //得到文件名并编码（防止中文文件名乱码）同样放入BodyPart  
                    multiPart.addBodyPart(bodyPart);  
                }
            }

            // 设置邮件消息的主要内容
            message.setContent(multiPart);

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            
            System.out.println("Sent message successfully....");
            result = true;
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return result;

	}
    // dannel modify a new method below:
	public static Properties hostSetUp(String mail_host) {
		Properties properties = System.getProperties();
		String host = null, port = null;
		
		switch (mail_host) {
		case "smtp.gmail.com":
			host = "smtp.gmail.com";
			port = "465";
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.starttls.enable","false");
			break;
		case "smtp.office365.com":
			host = "smtp.office365.com";
			port = "587";
			properties.put("mail.smtp.starttls.enable","true");
			properties.put("mail.smtp.ssl.enable", "false");
			break;
		case "smtp.mailgun.org":
			host = "smtp.mailgun.org";
			port = "587";
			properties.put("mail.smtp.starttls.enable","true");
			properties.put("mail.smtp.ssl.enable", "false");
			break;
		case "smtp.tom.com":
			host = "smtp.tom.com";
			port = "25";
			properties.put("mail.smtp.starttls.enable","false");
			properties.put("mail.smtp.ssl.enable", "false");
			break;
		default:
			break;
		}
		properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
//        properties.put("mail.smtp.starttls.enable","true");
//        properties.put("mail.smtp.port", "465");
//        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
		return properties;
	}

	public static void send(String to,String verifycode,int type) {
		// Recipient's email ID needs to be mentioned.
        //String to = "robinsonhood1978@gmail.com";

        // Sender's email ID needs to be mentioned
        //String from = "fynco.storage@gmail.com";
		final String from = "fyncotester@gmail.com";
        // Assuming you are sending email from through gmails smtp
        //String host = "smtp.gmail.com";
		String host = "smtp.gmail.com";
        // Get system properties
//        Properties properties = System.getProperties();
//
//        // Setup mail server
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port", "465");
//        properties.put("mail.smtp.ssl.enable", "true");
//        properties.put("mail.smtp.auth", "true");
		Properties properties = hostSetUp(host);
        

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                //return new PasswordAuthentication("fynco.storage@gmail.com", "fynco321");
            	return new PasswordAuthentication(from, "fynco@test");

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("[Fynco] - Verify your email address");

            // Now set the actual message
            //message.setText("This is actual message");
         // Send the actual HTML message.
            switch(type){
            	case 0:
            		message.setContent(
                            " <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + 
                            "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\"> \n" + 
                            "<head>\n" + 
                            "<style type=\"text/css\">\n" + 
                            " .link:link, .link:active, .link:visited {\n" + 
                            "       color:#2672ec !important;\n" + 
                            "       text-decoration:none !important;\n" + 
                            " }\n" + 
                            "\n" + 
                            " .link:hover {\n" + 
                            "       color:#4284ee !important;\n" + 
                            "       text-decoration:none !important;\n" + 
                            " }\n" + 
                            "</style>\n" + 
                            "<title></title>\n" + 
                            "</head>\n" + 
                            "<body>\n" + 
                            "<table dir=\"ltr\">\n" + 
                            "      <tr><td id=\"i1\" style=\"padding:0; font-family:'Segoe UI Semibold', 'Segoe UI Bold', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:17px; color:#707070;\">Fynco Team</td></tr>\n" + 
                            "      <tr><td id=\"i2\" style=\"padding:0; font-family:'Segoe UI Light', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:41px; color:#2672ec;\">Verify your email address</td></tr>\n" + 
                            "      <tr><td id=\"i4\" style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">To finish setting up your Fynco account password, we just need to make sure this is your email address.</td></tr>\n" + 
                            "      <tr><td style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">To verify your email address, use this security code: <span style=\"font-family:'Segoe UI Bold', 'Segoe UI Semibold', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:14px; font-weight:bold; color:#2a2a2a;\">"
                            +verifycode+ "</span></td></tr>\n" + 
                            "      <tr><td id=\"i6\" style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">If you did not request this code, you can safely ignore this email. Someone may have typed your email address by mistake.</td></tr>\n" + 
                            "      <tr><td style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">Thank you!</td></tr>\n" + 
                            "      <tr><td id=\"i8\" style=\"padding:0; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">Fynco account team</td></tr>\n" + 
                            "</table>\n" + 
                            "</body>\n" + 
                            "</html>",
                           "text/html");
            		break;
            	case 1:
            		message.setContent(
                            " <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + 
                            "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\"> \n" + 
                            "<head>\n" + 
                            "<style type=\"text/css\">\n" + 
                            " .link:link, .link:active, .link:visited {\n" + 
                            "       color:#2672ec !important;\n" + 
                            "       text-decoration:none !important;\n" + 
                            " }\n" + 
                            "\n" + 
                            " .link:hover {\n" + 
                            "       color:#4284ee !important;\n" + 
                            "       text-decoration:none !important;\n" + 
                            " }\n" + 
                            "</style>\n" + 
                            "<title></title>\n" + 
                            "</head>\n" + 
                            "<body>\n" + 
                            "<table dir=\"ltr\">\n" + 
                            "      <tr><td id=\"i1\" style=\"padding:0; font-family:'Segoe UI Semibold', 'Segoe UI Bold', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:17px; color:#707070;\">Fynco Team</td></tr>\n" + 
                            "      <tr><td id=\"i2\" style=\"padding:0; font-family:'Segoe UI Light', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:41px; color:#2672ec;\">your access code</td></tr>\n" + 
                            "      <tr><td id=\"i4\" style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">To setting up your Fynco account password, we just need to make sure this is your email address.</td></tr>\n" + 
                            "      <tr><td style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">To verify your email address, use this access code: <span style=\"font-family:'Segoe UI Bold', 'Segoe UI Semibold', 'Segoe UI', 'Helvetica Neue Medium', Arial, sans-serif; font-size:14px; font-weight:bold; color:#2a2a2a;\">"
                            +verifycode+ "</span></td></tr>\n" + 
                            "      <tr><td id=\"i6\" style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">If you did not request this code, you can safely ignore this email. Someone may have typed your email address by mistake.</td></tr>\n" + 
                            "      <tr><td style=\"padding:0; padding-top:25px; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">Thank you!</td></tr>\n" + 
                            "      <tr><td id=\"i8\" style=\"padding:0; font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif; font-size:14px; color:#2a2a2a;\">Fynco account team</td></tr>\n" + 
                            "</table>\n" + 
                            "</body>\n" + 
                            "</html>",
                           "text/html");
            			break;
            }
            
            

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

	}
	public static boolean yun_web_contact_mail(String realPath, String name, String email, String subject, String message, String[] atts) {
    	String html = StrUtil.readHtmlFile(realPath+"/f/mail/contact_mail.html");
    	html = html.replace("[[subject]]",subject);
    	html = html.replace("[[username]]",name);
    	html = html.replace("[[message]]",message);
    	html = html.replace("[[emailbox]]",email);
    	return send(subject, html ,atts);
	}
	public static boolean yun_web_quote_mail(String realPath, String subject, String freight_type, String weight, String departure, String destination, String email) {
    	String html = StrUtil.readHtmlFile(realPath+"/f/mail/quote_mail.html");
    	html = html.replace("[[subject]]",subject);
    	html = html.replace("[[freight_type]]",freight_type);
    	html = html.replace("[[weight]]",weight);
    	html = html.replace("[[departure]]",departure);
    	html = html.replace("[[destination]]",destination);
    	html = html.replace("[[emailbox]]",email);
    	return send(subject, html ,null);
	}
	public static boolean asynchronizedSendQuoteEmail(final String realPath, final String subject, final String freight_type, final String weight, final String departure, final String destination, final String email, final String[] atts) {
        Boolean flag = Boolean.FALSE;
        FutureTask<Boolean> futureTask = null;
        ExecutorService excutorService = Executors.newCachedThreadPool();
 
        // 执行任务
        futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SendMail.yun_web_quote_mail(realPath,subject,freight_type,weight,departure,destination,email);
            }
        });
        excutorService.submit(futureTask);
        try {
            // 任务没超时说明发送成功
            flag = futureTask.get(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } catch (TimeoutException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } finally {
            excutorService.shutdown();
        }
        return flag;
    }
	/**
     * 异步发送email
     * @author sun
     * @param toEmail
     * @param type
     * @return
     */
    public static boolean asynchronizedSendEmail(final String realPath, final String name, final String email, final String subject, final String message, final String[] atts) {
        Boolean flag = Boolean.FALSE;
        FutureTask<Boolean> futureTask = null;
        ExecutorService excutorService = Executors.newCachedThreadPool();
 
        // 执行任务
        futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SendMail.yun_web_contact_mail(realPath, name,email,subject, message, atts);
            }
        });
        excutorService.submit(futureTask);
        try {
            // 任务没超时说明发送成功
            flag = futureTask.get(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } catch (TimeoutException e) {
            futureTask.cancel(true);
            e.printStackTrace();
        } finally {
            excutorService.shutdown();
        }
        return flag;
    }
    public static void main(String[] args) {
    	//SendMail.send("robinsonhood1978@gmail.com", "12345678",0);
    	//(String host,final String from,final String passwd,String to,String subject,String htmlbody)
    	//String host = "smtp.taijicoin.nz";
    	/*String host = "smtp.mailgun.org";
    	//String from = "robin@taijicoin.nz";
    	String from = "postmaster@sandbox9650e85b669c41c18ca5f49c839e1be0.mailgun.org";
    	String passwd = "476396af3c781dedcf539f9ced87aa4c-e438c741-62b6fac3";
    	String to = "robinsonhood1978@gmail.com";*/
    	
    	String host = "smtp.tom.com";
    	String from = "bobpaul@tom.com";
    	String passwd = "niboul";
    	String to = "bobpaul@tom.com";
    	String subject = "Welcome you to our house";
    	String htmlbody = "hehehaha";
    	//String[] atts = {"/Users/mark/robin/sn.txt"};
    	String[] atts = null;
    	SendMail.send(host, from, passwd, to, subject, htmlbody,atts);
    }

}
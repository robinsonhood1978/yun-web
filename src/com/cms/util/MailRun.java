package com.cms.util;

public class MailRun implements Runnable {
	  private final String realPath;
	  private final String name;
	  private final String email;
	  private final String subject;
	  private final String message;
	  private final String[] atts;
      public MailRun(final String realPath, final String name, final String email,final String subject,final String message,final String[] atts) {
         this.realPath = realPath;
         this.name = name;
         this.email = email;
         this.subject = subject;
         this.message = message;
         this.atts = atts;
      }

      public void run() {
      }
   }
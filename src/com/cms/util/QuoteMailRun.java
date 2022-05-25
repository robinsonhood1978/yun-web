package com.cms.util;

public class QuoteMailRun implements Runnable {
	  private final String realPath;
	  private final String subject;
	  private final String freight_type;
	  private final String weight;
	  private final String departure;
	  private final String destination;
	  private final String email;
	  private final String[] atts;
      public QuoteMailRun(final String realPath, final String subject, final String freight_type, final String weight, final String departure, final String destination, final String email, final String[] atts) {
         this.realPath = realPath;
         
         this.subject = subject;
         this.freight_type = freight_type;
         this.weight = weight;
         this.departure = departure;
         this.destination = destination;
         this.email = email;
         
         this.atts = atts;
      }

      public void run() {
      }
   }
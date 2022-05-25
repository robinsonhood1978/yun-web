package com.cms.util;

import java.awt.*;   
import java.awt.image.*;   
import java.io.*;   
import javax.imageio.*;   
import java.awt.font.*;   
import java.awt.geom.*;   
  
public class CreateImage    
{   
    public static void main(String[] args) throws Exception   
    {   
        int width = 100;   
        int height = 100;   
        String s = "中华人民";   
           
        File file = new File("c:/image.jpg");   
           
        Font font = new Font("Serif", Font.BOLD, 20);   
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);   
        Graphics2D g2 = (Graphics2D)bi.getGraphics();   
        g2.setBackground(Color.WHITE);   
        g2.clearRect(0, 0, width, height);   
        g2.setPaint(Color.BLACK);   
           
        FontRenderContext context = g2.getFontRenderContext();   
        Rectangle2D bounds = font.getStringBounds(s, context);   
        double x = (width - bounds.getWidth()) / 2;   
        double y = (height - bounds.getHeight()) / 2;   
        double ascent = -bounds.getY();   
        double baseY = y + ascent;   
           
        g2.drawString(s, (int)x, (int)baseY);   
           
        ImageIO.write(bi, "jpg", file);   
    }   
}    

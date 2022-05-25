package com.cms.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
* JAVA 画图(生成文字水印)
* @author   啄木鸟：聪聪
*  
*/
public class ImgCreate {

/**
* @param str
*            生产的图片文字
* @param filePath
*            定义图片保存路径
* @param width
*            定义生成图片宽度
* @param height
*            定义生成图片高度
* @return
*/
public String create(String str, String filePath, int width, int height) {

   String fileName = System.currentTimeMillis() + ".jpg";
   String path = filePath + "/" + fileName;
   File file = new File(path);

   BufferedImage bi = new BufferedImage(width, height,
     BufferedImage.TYPE_INT_RGB);
  
   Graphics2D g2 = (Graphics2D) bi.getGraphics();
   g2.setBackground(Color.WHITE);
   g2.clearRect(0, 0, width, height);

   /** 设置生成图片的文字样式 * */
   Font font = new Font("黑体", Font.BOLD, 25);
   g2.setFont(font);
   g2.setPaint(Color.BLACK);

   /** 设置字体在图片中的位置 在这里是居中* */
   FontRenderContext context = g2.getFontRenderContext();
   Rectangle2D bounds = font.getStringBounds(str, context);
   double x = (width - bounds.getWidth()) / 2;
   double y = (height - bounds.getHeight()) / 2;
   double ascent = -bounds.getY();
   double baseY = y + ascent;

   /** 防止生成的文字带有锯齿 * */
   g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

   /** 在图片上生成文字 * */
   g2.drawString(str, (int) x, (int) baseY);

   try {
    ImageIO.write(bi, "jpg", file);
   } catch (IOException e) {
    e.printStackTrace();
   }
   return file.getPath();
}

public static void main(String[] args) {
   ImgCreate create = new ImgCreate();
   System.out.println(create.create("中华人民共和国万岁！", "c:/", 500, 38));
}
}
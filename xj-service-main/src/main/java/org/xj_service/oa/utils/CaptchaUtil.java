package org.xj_service.oa.utils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 验证码工具类
 */
@Component
public class CaptchaUtil {

    @Autowired
    private DefaultKaptcha captchaProducer;

    /**
     * 生成带干扰线的4位数字验证码图片
     * @param captchaText 验证码文本
     * @return Base64编码的图片字符串
     */
    public String generateCaptchaImage(String captchaText) {
        try {
            // 创建图片
            int width = 120;
            int height = 45;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 设置背景色
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // 设置字体
            g.setFont(new Font("Arial", Font.BOLD, 30));

            // 绘制数字
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(captchaText)) / 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(captchaText, x, y);

            // 添加干扰线
            Random random = new Random();
            g.setColor(Color.GRAY);
            for (int i = 0; i < 5; i++) {
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                g.drawLine(x1, y1, x2, y2);
            }

            // 添加噪点
            g.setColor(Color.GRAY);
            for (int i = 0; i < 30; i++) {
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                g.drawOval(x1, y1, 1, 1);
            }

            g.dispose();

            // 转换为Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] bytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


//    /**
//     * 生成验证码并直接写入响应
//     * @param response HTTP响应对象
//     * @param captchaText 验证码文本
//     */
//    public void generateCaptchaImage(HttpServletResponse response, String captchaText) {
//        try {
//            // 设置响应头
//            response.setDateHeader("Expires", 0);
//            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
//            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
//            response.setHeader("Pragma", "no-cache");
//            response.setContentType("image/png");
//
//            // 创建验证码图片
//            BufferedImage image = captchaProducer.createImage(captchaText);
//
//            // 输出图片到响应流
//            ServletOutputStream out = response.getOutputStream();
//            javax.imageio.ImageIO.write(image, "jpg", out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 生成4位随机数字验证码
     * @return 4位数字验证码
     */
    public String generateCaptchaText() {
        Random random = new Random();
        int captchaNumber = random.nextInt(9000) + 1000; // 生成1000-9999之间的4位数字
        System.out.println("验证码是：" + captchaNumber);
        return String.valueOf(captchaNumber);
    }

}

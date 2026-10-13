package com.gk.iam.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CaptchaUtil {

    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static class CaptchaImage {
        private final String code;
        private final String base64DataUri;

        public CaptchaImage(String code, String base64DataUri) {
            this.code = code;
            this.base64DataUri = base64DataUri;
        }

        public String getCode() {
            return code;
        }

        public String getBase64DataUri() {
            return base64DataUri;
        }
    }

    public static CaptchaImage generateCaptcha(int width, int height, int codeLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        String code = sb.toString();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 开启抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 背景渐变色
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(245, 247, 250),
                width, height, new Color(228, 235, 245)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // 绘制随机干扰曲线
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 4; i++) {
            g2.setColor(new Color(RANDOM.nextInt(120) + 80, RANDOM.nextInt(120) + 80, RANDOM.nextInt(120) + 80, 160));
            int x1 = RANDOM.nextInt(width / 3);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width / 2) + width / 2;
            int y2 = RANDOM.nextInt(height);
            int ctrlX = RANDOM.nextInt(width);
            int ctrlY = RANDOM.nextInt(height);
            g2.draw(new java.awt.geom.QuadCurve2D.Float(x1, y1, ctrlX, ctrlY, x2, y2));
        }

        // 绘制噪点
        for (int i = 0; i < 40; i++) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            g2.setColor(new Color(RANDOM.nextInt(160), RANDOM.nextInt(160), RANDOM.nextInt(160), 120));
            g2.fillOval(x, y, 2, 2);
        }

        // 绘制字符
        Font[] fonts = new Font[]{
                new Font("Arial", Font.BOLD, 26),
                new Font("Verdana", Font.BOLD, 25),
                new Font("Tahoma", Font.BOLD, 26)
        };

        int charStep = (width - 24) / codeLength;
        for (int i = 0; i < codeLength; i++) {
            Font font = fonts[RANDOM.nextInt(fonts.length)];
            g2.setFont(font);

            // 较深的文字颜色，确保对比度
            Color charColor = new Color(RANDOM.nextInt(80) + 20, RANDOM.nextInt(80) + 20, RANDOM.nextInt(120) + 40);
            g2.setColor(charColor);

            char c = code.charAt(i);
            int x = 12 + i * charStep + RANDOM.nextInt(6) - 3;
            int y = height / 2 + 9 + RANDOM.nextInt(6) - 3;

            // 轻微旋转字符
            AffineTransform originalTransform = g2.getTransform();
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4; // -0.2 ~ +0.2 rad
            g2.rotate(angle, x + 6, y - 10);
            g2.drawString(String.valueOf(c), x, y);
            g2.setTransform(originalTransform);
        }

        g2.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new CaptchaImage(code, "data:image/png;base64," + base64);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
}


package com.ruoyi.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * ID加密工具类
 * 用于对ID进行加密和解密，适用于分享场景
 * 
 * @author ruoyi
 */
public class IdEncryptUtils
{
    /**
     * 加密盐值，可以自定义修改
     */
    private static final String SALT = "ruoyi_share_2026";

    /**
     * 加密ID
     * 
     * @param id 原始ID
     * @return 加密后的字符串
     */
    public static String encrypt(Integer id)
    {
        if (id == null)
        {
            return null;
        }
        
        try
        {
            // 将ID与盐值组合
            String original = id + ":" + SALT;
            // Base64编码
            String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
            // 移除可能存在的填充字符，使URL更友好
            return encoded.replace("=", "");
        }
        catch (Exception e)
        {
            throw new RuntimeException("ID加密失败", e);
        }
    }

    /**
     * 解密ID
     * 
     * @param encryptedId 加密后的ID字符串
     * @return 原始ID
     */
    public static Integer decrypt(String encryptedId)
    {
        if (StringUtils.isEmpty(encryptedId))
        {
            return null;
        }
        
        try
        {
            // 恢复Base64填充字符
            String base64Str = encryptedId;
            int padding = 4 - (base64Str.length() % 4);
            if (padding < 4)
            {
                for (int i = 0; i < padding; i++)
                {
                    base64Str += "=";
                }
            }
            
            // Base64解码
            byte[] decoded = Base64.getDecoder().decode(base64Str);
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);
            
            // 验证盐值并提取ID
            if (decodedStr.endsWith(":" + SALT))
            {
                String idStr = decodedStr.substring(0, decodedStr.length() - SALT.length() - 1);
                return Integer.parseInt(idStr);
            }
            else
            {
                throw new IllegalArgumentException("无效的加密ID");
            }
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("ID格式错误", e);
        }
        catch (Exception e)
        {
            throw new RuntimeException("ID解密失败", e);
        }
    }
}

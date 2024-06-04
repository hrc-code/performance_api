package com.example.workflow.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTHelper {//过期时间
    public static final long EXPIRE_TIME =   30 * 60 * 1000;//默认30分钟
    //私钥
    private static final String TOKEN_SECRET = "privateKey";

    /**
     * 生成token，自定义过期时间 毫秒
     *
     * @param **username**
     * @param **password**
     * @return
     */
    public static String createToken(long expireDate, Map<String, String> payload) {
        try {
            // 设置过期时间
            Date date = new Date(expireDate);
            // 私钥和加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            // 设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("Type", "Jwt");
            header.put("alg", "HS256");
            // 返回token字符串
            JWTCreator.Builder builder = JWT.create()
                    .withHeader(header)
                    .withExpiresAt(date);
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                builder.withClaim(entry.getKey(), entry.getValue());
            }


            return builder.sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long generateExpireDate(long duration) {
        return System.currentTimeMillis() + duration;
    }

    /**
     * 检验token是否正确
     *
     * @param **token**
     * @return
     */
    public static Map<String, Claim> verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaims();
        } catch (Exception e) {
            return null;
        }
    }


}

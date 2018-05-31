package com.teamface.common.util.jwt;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class TokenMgr
{
    
    public static SecretKey generalKey()
    {
        byte[] encodedKey = Base64.decode(Constant.JWT_SECERT);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }
    
    /**
     * ǩ��JWT
     * 
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     * @throws Exception
     */
    public static String createJWT(String accountId, String employeeId, String companyId, String signId, long ttlMillis)
    {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        SecretKey secretKey = generalKey();
        JwtBuilder builder =
            Jwts.builder().setId(accountId).setSubject(employeeId).setAudience(companyId).setIssuer(signId).setIssuedAt(now).signWith(signatureAlgorithm, secretKey);
        if (ttlMillis >= 0)
        {
            long expMillis = nowMillis + ttlMillis;
            Date expDate = new Date(expMillis);
            builder.setExpiration(expDate);
        }
        return builder.compact();
    }
    
    public static CheckResult validateJWT(String jwtStr)
    {
        CheckResult checkResult = new CheckResult();
        Claims claims = null;
        try
        {
            claims = parseJWT(jwtStr);
            checkResult.setSuccess(true);
            checkResult.setClaims(claims);
        }
        catch (ExpiredJwtException e)
        {
            checkResult.setErrCode(Constant.JWT_ERRCODE_EXPIRE);
            checkResult.setSuccess(false);
        }
        catch (SignatureException e)
        {
            checkResult.setErrCode(Constant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        }
        catch (Exception e)
        {
            checkResult.setErrCode(Constant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        }
        return checkResult;
    }
    
    public static Claims parseJWT(String jwt)
        throws Exception
    {
        SecretKey secretKey = generalKey();
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt).getBody();
    }
    
    public static String generalSubject(SubjectModel sub)
    {
        return null; // GsonUtil.objectToJsonStr(sub);
    }
    
    public static InfoVo obtainInfo(String jwt)
    {
        SecretKey secretKey = generalKey();
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt).getBody();
        InfoVo info = new InfoVo(claims.getId() == null ? null : Long.parseLong(claims.getId()), claims.getSubject() == null ? null : Long.parseLong(claims.getSubject()),
            claims.getAudience() == null ? null : Long.parseLong(claims.getAudience()), claims.getIssuer() == null ? null : Long.parseLong(claims.getIssuer()));
        return info;
    }
    
    public static void main(String[] args)
        throws Exception
    {
        createJWT("1", "1", "1", "1", -1);

        InfoVo info = TokenMgr.obtainInfo(
            "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyMSIsInN1YiI6IjEiLCJhdWQiOiIxMyIsImlzcyI6IjEwMDIzIiwiaWF0IjoxNTI2OTcwOTA0fQ.fq26nc3lZ8hXf4AYwzQzvC68QzmFAm4ls1Dv3CYPoKg");
        System.out.println(info.getAccountId());
        System.out.println(info.getCompanyId());
        System.out.println(info.getEmployeeId());
        System.out.println(info.getSignId());
        
    }
    
}

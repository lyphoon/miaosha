package com.geekq.miaosha.redis;

import com.alibaba.fastjson.JSON;
import com.geekq.miaosha.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RedisService {
	
	@Autowired
	JedisPool jedisPool;

	private <T> T commonTemplate(RedisCallBackService<T> redisCallBack, Jedis jedis){
		T result = null;
		try {
			result = redisCallBack.execute();
		}catch (Exception e){
			log.error("redis操作失败",e);
		} finally {
			returnToPool(jedis);
		}
		return  result;

	}


	/**
	 * 设置失效时间
	 * @param key
	 * @param value
	 * @return
	 */
	public Long setnx(String key ,String value){
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.setnx(key,value), jedis);

		/*Jedis jedis =null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.setnx(key,value);
		}catch (Exception e){
			log.error("expire key:{} error",key,e);
		} finally {
			returnToPool(jedis);
		}
		return  result;*/

	}
	/**
	 * 设置key的有效期，单位是秒
	 * @param key
	 * @param exTime
	 * @return
	 */
	public Long expire(String key,int exTime){
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.expire(key,exTime), jedis);

		/*Jedis jedis = null;
		Long result = null;
		try {
			jedis =  jedisPool.getResource();
			result = jedis.expire(key,exTime);
		} catch (Exception e) {
			log.error("expire key:{} error",key,e);
			jedisPool.returnBrokenResource(jedis);
			return result;
		}
		jedisPool.returnResource(jedis);
		return result;*/
	}

	/**
	 * 获取当个对象
	 * */
	public <T> T get(KeyPrefix prefix, String key,  Class<T> clazz) {
		return BeanUtil.stringToBean(get(prefix.getPrefix()+key), clazz);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			 //生成真正的key
			 String realKey  = prefix.getPrefix() + key;
			 String  str = jedis.get(realKey);
			 T t =  stringToBean(str, clazz);
			 return t;
		 }finally {
			  returnToPool(jedis);
		 }*/
	}

    public  String get(String key){
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.get(key), jedis);

        /*Jedis jedis = null;
        String result = null;
        try {
            jedis =  jedisPool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("expire key:{} error",key,e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;*/
    }


    public  String getset(String key,String value){
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.getSet(key, value), jedis);

        /*Jedis jedis = null;
        String result = null;
        try {
            jedis =  jedisPool.getResource();
            result = jedis.getSet(key,value);
        } catch (Exception e) {
            log.error("expire key:{} error",key,e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;*/
    }
	/**
	 * 设置对象
	 *
	 * Boolean b = null;
	 * boolean c = b;  //这个地方会出异常， 不推荐使用小写的
	 * System.out.println(c);
	 * */
	public <T> Boolean set(KeyPrefix prefix, String key,  T value) {
		String str = BeanUtil.beanToString(value);
		if(str == null || str.length() <= 0) {
			return Boolean.FALSE;
		}

		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> {
			String realKey  = prefix.getPrefix() + key;
			int seconds =  prefix.expireSeconds();
			if(seconds <= 0) {
				jedis.set(realKey, str);
			}else {
				jedis.setex(realKey, seconds, str);
			}
			return Boolean.TRUE;
		}, jedis);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			 String str = beanToString(value);
			 if(str == null || str.length() <= 0) {
				 return false;
			 }
			//生成真正的key
			 String realKey  = prefix.getPrefix() + key;
			 int seconds =  prefix.expireSeconds();
			 if(seconds <= 0) {
				 jedis.set(realKey, str);
			 }else {
				 jedis.setex(realKey, seconds, str);
			 }
			 return true;
		 }finally {
			  returnToPool(jedis);
		 }*/
	}
	
	/**
	 * 判断key是否存在
	 * */
	public Boolean exists(KeyPrefix prefix, String key) {
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.exists(prefix.getPrefix() + key), jedis);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			 String realKey  = prefix.getPrefix() + key;
			return  jedis.exists(realKey);
		 }finally {
			  returnToPool(jedis);
		 }*/
	}
	
	/**
	 * 删除
	 * */
	public Boolean delete(KeyPrefix prefix, String key) {
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> {
			Long ret =  jedis.del(prefix.getPrefix() + key);
			return ret > 0;
		}, jedis);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			String realKey  = prefix.getPrefix() + key;
			long ret =  jedis.del(realKey);
			return ret > 0;
		 }finally {
			  returnToPool(jedis);
		 }*/
	}
	
	/**
	 * 增加值
	 * */
	public Long incr(KeyPrefix prefix, String key) {
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.incr(prefix.getPrefix() + key), jedis);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			 String realKey  = prefix.getPrefix() + key;
			return  jedis.incr(realKey);
		 }finally {
			  returnToPool(jedis);
		 }*/
	}
	
	/**
	 * 减少值
	 * */
	public <T> Long decr(KeyPrefix prefix, String key) {
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.decr(prefix.getPrefix() + key), jedis);

		 /*Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			 String realKey  = prefix.getPrefix() + key;
			return  jedis.decr(realKey);
		 }finally {
			  returnToPool(jedis);
		 }*/
	}

    public  Long del(String key){
		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> jedis.del(key), jedis);

        /*Jedis jedis = null;
        Long result = null;
        try {
            jedis =  jedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error",key,e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;*/
    }


	public Boolean delete(KeyPrefix prefix) {
		//放在Jedis前面，减少连接
		if(prefix == null) {
			return false;
		}


		Jedis jedis = jedisPool.getResource();
		return commonTemplate(() -> {
			List<String> keys = scanKeys(prefix.getPrefix(), jedis);
			if(keys==null || keys.size() <= 0) {
				return true;
			}

			jedis.del(keys.toArray(new String[0]));
			return true;
		}, jedis);

		/*if(prefix == null) {
			return false;
		}
		List<String> keys = scanKeys(prefix.getPrefix());
		if(keys==null || keys.size() <= 0) {
			return true;
		}
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(keys.toArray(new String[0]));
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(jedis != null) {
				jedis.close();
			}
		}*/
	}

	//这个仅有一个地方使用
	private List<String> scanKeys(String key, Jedis jedis) {
		List<String> keys = new ArrayList<String>();
		String cursor = "0";
		ScanParams sp = new ScanParams();
		sp.match("*"+key+"*");
		sp.count(100);
		do{
			ScanResult<String> ret = jedis.scan(cursor, sp);
			List<String> result = ret.getResult();
			if(result!=null && result.size() > 0){
				keys.addAll(result);
			}
			//再处理cursor
			cursor = ret.getStringCursor();
		}while(!cursor.equals("0"));

		return keys;
	}
	


	private void returnToPool(Jedis jedis) {
		 if(jedis != null) {
			 jedis.close();
		 }
	}

}

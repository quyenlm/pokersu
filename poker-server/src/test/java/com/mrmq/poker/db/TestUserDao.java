package com.mrmq.poker.db;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mrmq.poker.db.dao.PkUserDao;
import com.mrmq.poker.db.entity.PkUser;

import junit.framework.TestCase;

public class TestUserDao extends TestCase {
	private ClassPathXmlApplicationContext context;
	private PkUserDao pkUsersDao;
	
	@Before
	public void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("classpath:poker-context.xml");
		pkUsersDao = (PkUserDao) context.getBean("pkUserDao");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInsert() {
		PkUser item = new PkUser();
		item.setUserId(1);
		item.setLogin("user1");
		item.setPass("e10adc3949ba59abbe56e057f20f883e");
		
		item.setUserName("hhhhhhhh");
		item.setAddress("hhhhhhhh");
		item.setCity("hhhhhhhh");
		item.setState("hhhhhhhh");
		item.setZipcode("hhhhhhhh");
		item.setCountry("VN");
		item.setPhone("hhhhhhhh");
		item.setEmail("hhhhhhhh");
		item.setPubKey("hhhhhhhh");
		item.setComment("hhhhhhhh");
		
		item.setBalance(new BigDecimal("100000"));
		item.setPrevBalance(new BigDecimal("0"));
		item.setCredit(new BigDecimal("0"));
		item.setTaxes(new BigDecimal("0"));
		item.setUserGroup("DEMO");
		item.setCurrency("VND");
		item.setStatus(1);
		item.setRegDate(new Timestamp(System.currentTimeMillis()));
		item.setUpdateDate(item.getRegDate());
		
		pkUsersDao.save(item);
		System.out.println(item);
		
		item = pkUsersDao.findById(PkUser.class, item.getUserId());
		
		assertNotNull(item);
	}
	
	@Test
	public void testUpdate() {
		List<PkUser> listItem = pkUsersDao.findByProperty(PkUserDao.STATUS, 1);
		assertNotNull(listItem);
		assertTrue(listItem.size() > 0);
		
		PkUser item = listItem.get(0);
		item.setBalance(new BigDecimal("1000000"));
		
		item = pkUsersDao.merge(item);
		item = pkUsersDao.findById(PkUser.class, item.getUserId());
		
		assertEquals(1000000, item.getBalance().intValue());
	}
}

package com.mrmq.poker.db;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mrmq.poker.db.dao.PkCashflowDao;
import com.mrmq.poker.db.entity.PkCashflow;

import junit.framework.TestCase;


public class TestCashFlowDao extends TestCase {
	private ClassPathXmlApplicationContext context;
	private PkCashflowDao cashflowDAO;
	
	private final int userID = 100000;
	
	@Before
	public void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("classpath:poker-context.xml");
		cashflowDAO = (PkCashflowDao) context.getBean("pkCashflowDao");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInsert() {
		PkCashflow item = new PkCashflow();
		item.setUserId(userID);
		
		item.setAmount(new BigDecimal("10000"));
		item.setCashBalance(new BigDecimal("10000"));
		item.setPreBalance(new BigDecimal("0"));
		item.setCurrency("VND");
		item.setInputDate(new Timestamp(System.currentTimeMillis()));
		item.setUpdateDate(item.getInputDate());
		item.setSource(1);
		item.setStatus(1);
		item.setTaxes(new BigDecimal("0"));
		item.setType(1);
		
		cashflowDAO.save(item);
		System.out.println(item);
		
		item = cashflowDAO.findById(PkCashflow.class, item.getCashflowId());
		
		assertNotNull(item);
	}
	
	@Test
	public void testUpdate() {
		List<PkCashflow> listItem = cashflowDAO.findByProperty(PkCashflowDao.USER_ID, userID);
		assertNotNull(listItem);
		assertTrue(listItem.size() > 0);
		
		PkCashflow item = listItem.get(0);
		item.setAmount(new BigDecimal("0"));
		
		item = cashflowDAO.merge(item);
		item = cashflowDAO.findById(PkCashflow.class, item.getCashflowId());
		
		assertEquals(0, item.getAmount().intValue());
	}
}

package com.whizupp.jpims;

import com.whizupp.jpims.repository.ProductionBatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WhizuppApplicationTests {

	@Autowired
	private ProductionBatchRepository batchRepository;

	@Test
	void printProductionBatches() {
		System.out.println("--- START PRINTING PRODUCTION BATCHES ---");
		batchRepository.findAll().forEach(b -> {
			System.out.println("BATCH: ID=" + b.getId() 
				+ ", number=" + b.getBatchNumber()
				+ ", product=" + b.getProductName()
				+ ", status=" + b.getStatus());
		});
		System.out.println("--- END PRINTING PRODUCTION BATCHES ---");
	}

}

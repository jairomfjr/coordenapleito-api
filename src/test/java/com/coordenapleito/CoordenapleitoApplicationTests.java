package com.coordenapleito;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = CoordenapleitoApplication.class,
		properties = {
				"storage.type=local",
				"sps.coordenapleito.storage.local.anexos=${java.io.tmpdir}/coordenapleito-test-anexos",
				"app.demanda-municipio.expiracao-job.enabled=false"
		})
class CoordenapleitoApplicationTests {

	@Test
	void contextLoads() {
	}
}


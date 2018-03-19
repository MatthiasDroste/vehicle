package com.droste.vehicle.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.droste.vehicle.VehicleApplication;
import com.droste.vehicle.model.Position;
import com.droste.vehicle.model.PositionRepository;
import com.droste.vehicle.model.Session;
import com.droste.vehicle.model.SessionRepository;
import com.droste.vehicle.model.Vehicle;
import com.droste.vehicle.model.VehicleRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VehicleApplication.class)
@WebAppConfiguration
public class SessionRestControllerTest {

	static final String VIN = "WLQBNAL7EM14E3N";
	static final String VIN2 = "WLQBNAL7EM14E32";
	static final String SESSION_ID = "6bc6a660dfef4010ded079865f358e30";
	static final String SESSION_ID2 = "ef0f515c3b19e177fb67a5b51b736d71";
	static final long TIMESTAMP = 1520106445654L;
	static final long RECENT = 1519990629975L;
	static final long MIDDLE = 1519990625885L;
	static final long OLDEST = 1519990621975L;

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private Position testPosition;
	private Vehicle testVehicle;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private VehicleRepository vehicleRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();
		this.testVehicle = vehicleRepository.save(new Vehicle(VIN));
		Session session = new Session(SESSION_ID, RECENT, testVehicle);
		session = sessionRepository.save(session);
		assertTrue(RECENT > MIDDLE);
		assertTrue(MIDDLE > OLDEST);
		this.testPosition = new Position(RECENT, 48.1167D, 11.5400D, 252, session);
		positionRepository.save(testPosition);
		Position testPosition2 = new Position(OLDEST, 48.1167D, 11.5394D, 291, session);
		positionRepository.save(testPosition2);
		positionRepository.save(new Position(MIDDLE, 48.1168D, 11.5397D, 291, session));

		Session session2 = new Session(SESSION_ID2, TIMESTAMP, testVehicle);
		session2 = sessionRepository.save(session2);
		positionRepository.save(new Position(1520106445654L, 48.1471D, 11.5512D, 228, session2));
		positionRepository.save(new Position(1520106414654L, 48.1487D, 11.5522D, 107, session2));
		positionRepository.save(new Position(1520106503194L, 48.1450D, 11.5527D, 115, session2));

	}

	@After
	public void tearDown() throws Exception {
		this.positionRepository.deleteAllInBatch();
		this.sessionRepository.deleteAllInBatch();
	}

	/** get all sessions of a vehicle in correct ordering */
	@Test
	public void getAllSessions() throws Exception {
		mockMvc.perform(get("/vehicle/" + VIN + "/session")).andExpect(status().isOk())
				.andExpect(content().contentType(contentType)).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].positions", hasSize(3))).andExpect(jsonPath("$[0].sessionId", is(SESSION_ID)))
				.andExpect(jsonPath("$[0].positions[0].timestamp", is(RECENT)))
				.andExpect(jsonPath("$[0].positions[1].timestamp", is(MIDDLE)))
				.andExpect(jsonPath("$[0].positions[2].timestamp", is(OLDEST)));
	}

	@Test
	public void getAllSessionsVehicleNotFound() throws Exception {
		mockMvc.perform(get("/vehicle/" + "wrongVIN" + "/session")).andExpect(status().isNotFound()).andReturn();
	}

	/**
	 * Get a single session as an ordered list of the received positions by
	 * timestamp
	 */
	@Test
	public void getPositionsOfSession() throws Exception {
		mockMvc.perform(get("/vehicle/session/" + SESSION_ID)).andExpect(status().isOk())
				.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.sessionId", is(SESSION_ID)))
				.andExpect(jsonPath("$.positions", hasSize(3)))
				.andExpect(jsonPath("$.positions[0].timestamp", is(RECENT)))
				.andExpect(jsonPath("$.positions[1].timestamp", is(MIDDLE)))
				.andExpect(jsonPath("$.positions[2].timestamp", is(OLDEST)));
	}

	@Test
	public void getPositionsOfNonExistingSession() throws Exception {
		MvcResult result = mockMvc.perform(get("/vehicle/session/" + "nonsenseId")).andExpect(status().isNotFound())
				.andReturn();
		System.out.println("Positions of Sessions------------" + result.getResponse().getContentAsString());
	}

	@SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}

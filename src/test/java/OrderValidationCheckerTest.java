import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.models.Order;
import uk.ac.ed.inf.pizzadronz.models.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.models.Restaurant;
import uk.ac.ed.inf.pizzadronz.service.OrderValidationService;

import java.util.List;

import static org.mockito.Mockito.*;

class OrderValidationCheckerTest {

    @Test
    void testValidateOrders() throws Exception {
        assert(true);
    }
}

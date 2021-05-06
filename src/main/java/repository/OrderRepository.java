package repository;

import com.mongodb.client.MongoCollection;
import model.order.Order;
import org.bson.types.ObjectId;

import java.util.Optional;

public class OrderRepository extends MongoRepository<Order> {

    public OrderRepository(MongoCollection<Order> collection) {
        super(collection);
    }

    public Optional<Order> get(ObjectId id, String issuer) {
        return super.get(id)
                .stream()
                .filter(order -> order.getIssuer().getEmail().equals(issuer))
                .findFirst();
    }
}

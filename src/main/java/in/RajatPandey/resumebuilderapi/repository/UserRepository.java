package in.RajatPandey.resumebuilderapi.repository;

import in.RajatPandey.resumebuilderapi.Documents.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {

    Optional<User> findByEmail (String email);

    Boolean existsByEmail(String email);
}

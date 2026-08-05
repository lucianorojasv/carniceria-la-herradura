package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.CustomerRequest;
import pe.laherradura.entity.Customer;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.CustomerRepository;
import java.util.List;
@Service @Transactional
public class CustomerService {
 private final CustomerRepository repo;
 public CustomerService(CustomerRepository r){repo=r;}
 @Transactional(readOnly=true) public List<Customer> list(){return repo.findTop100ByOrderByCreatedAtDesc();}
 public Customer save(Long id,CustomerRequest r){
   Customer c=id==null?repo.findByPhone(clean(r.phone())).orElseGet(Customer::new):repo.findById(id).orElseThrow(()->new NotFoundException("Cliente no encontrado"));
   c.setName(r.name().trim());c.setPhone(clean(r.phone()));c.setAddress(r.address());c.setReference(r.reference());c.setZone(r.zone());c.setConsentMarketing(r.consentMarketing());return repo.save(c);
 }
 public Customer getOrCreate(String phone,String name){String p=clean(phone);return repo.findByPhone(p).orElseGet(()->{Customer c=new Customer();c.setPhone(p);c.setName(name==null||name.isBlank()?"Cliente WhatsApp":name.trim());return repo.save(c);});}
 public String clean(String phone){return phone==null?"":phone.replaceAll("[^0-9]","");}
}

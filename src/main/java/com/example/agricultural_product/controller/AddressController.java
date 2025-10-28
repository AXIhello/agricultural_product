-- ...existing code...

-- 专家工作时间段（新增）
CREATE TABLE IF NOT EXISTS `tb_expert_working_slots` (
  `slot_id` INT NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `expert_id` BIGINT NOT NULL COMMENT '专家用户ID，关联 users.user_id',
  `work_date` DATE NOT NULL COMMENT '日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `capacity` INT NOT NULL DEFAULT 1 COMMENT '可预约名额',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约数',
  `status` ENUM('open','closed') NOT NULL DEFAULT 'open' COMMENT '是否开放预约',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`slot_id`),
  INDEX `idx_expert_date`(`expert_id`, `work_date`),
  CONSTRAINT `fk_slot_expert_user` FOREIGN KEY (`expert_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家工作时间段';


ALTER TABLE `tb_expert_consultation`
  ADD COLUMN `slot_id` INT NULL COMMENT '工作时间段ID，关联 tb_expert_working_slots.slot_id' AFTER `expert_id`,
  ADD INDEX `idx_slot_farmer` (`slot_id`, `farmer_id`),
  ADD CONSTRAINT `fk_consultation_slot` FOREIGN KEY (`slot_id`) REFERENCES `tb_expert_working_slots` (`slot_id`) ON DELETE RESTRICT ON UPDATE CASCADE;



import com.example.agricultural_product.pojo.Address;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.AddressService;
import com.example.agricultural_product.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    // 获取单个地址详情
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddress(HttpServletRequest request,
                                              @PathVariable Integer id) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Address saved = addressService.getById(id);
        return ResponseEntity.ok(saved);
    }


    // 1. 新增地址
    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);
        address.setUserId(userId);

        Address saved = addressService.addAddress(address);
        return ResponseEntity.ok(saved);
    }

    // 2. 查询当前用户的所有地址
    @GetMapping("/user")
    public ResponseEntity<List<Address>> getUserAddresses(HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    // 3. 修改地址
    @PutMapping("/update/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Integer addressId,
                                                 @RequestBody Address address,
                                                 HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);
        address.setUserId(userId);

        Address updated = addressService.updateAddress(addressId, address);
        return ResponseEntity.ok(updated);
    }

    // 4. 删除地址
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId,
                                              HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    // 5. 设置默认地址
    @PutMapping("/set-default/{addressId}")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable Integer addressId,
                                                  HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}

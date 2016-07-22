package hello;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import dao.BaughtItemBig;
import dao.MoneyBig;
import dao.ValuesGnomeBig;
import dao.ValuesItemBig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by a on 01.11.15.
 */
@Repository
@Transactional
@Slf4j
public class TemplatesBig {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public ValuesGnomeBig showValuesGnome(String gnome_id) {
        String sql = "select gnome_name, gnome_money from gnomes where gnome_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{gnome_id}, new MappersGnomeBig());
    }

    public List<ValuesItemBig> showValuesItem(String gnome_id) {
        String sql = "select items.item_name, sales.quantity from gnomes, items, sales where" +
                " gnomes.gnome_id = sales.gnome_id  and sales.item_id = items.item_id and gnomes.gnome_id =?";
        return jdbcTemplate.query(sql, new MappersItemBig(), gnome_id);
    }

    public MoneyBig getMoney(String gnome_id) {
        String sql = "SELECT gnome_money FROM gnomes WHERE gnome_id = ?";
        return (MoneyBig) jdbcTemplate.queryForObject(sql, new Object[]{gnome_id}, new MapperMoneyBig());
    }

    public List<BaughtItemBig> getBaughtItem(String gnome_id) {
        String sql = "select item_id, quantity from sales where gnome_id=?";
        return jdbcTemplate.query(sql, new MapperBaughtItemBig(), gnome_id);
    }


    @Transactional(rollbackFor = Exception.class)
    public void buyItemNew(String gnome_id, String item_id, BigDecimal itemPrice) throws Exception {

        try {

            String sqlGiveMoney = "UPDATE gnomes SET gnome_money=gnome_money-? WHERE gnome_id=?";
            jdbcTemplate.update(sqlGiveMoney, itemPrice, gnome_id);

//            error();

            String sqlGetItem = "insert into sales (gnome_id, item_id, quantity) values (?, ?, 1);";
            jdbcTemplate.update(sqlGetItem, gnome_id, item_id);

        } catch (Exception dae) {
            log.error("Error in creating record, rolling back");
            dae.printStackTrace();
            throw dae;
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void buyItemOld(String gnome_id, String item_id, BigDecimal itemPrice) throws Exception {

        try {
            String sqlIncQuantity = "update sales set quantity=quantity+1 where gnome_id=?"
                    + " and item_id=?";
            jdbcTemplate.update(sqlIncQuantity, gnome_id, item_id);

            error();

            String sqlGiveMoney = "UPDATE gnomes SET gnome_money=gnome_money-?"
                    + " WHERE gnome_id=?";
            jdbcTemplate.update(sqlGiveMoney, itemPrice, gnome_id);


        } catch (Exception dae) {
            log.error("Error in creating record, rolling back");
            dae.printStackTrace();
            throw dae;
        }
    }

    public void error() throws Exception {
        throw new Exception();
    }

    @Transactional(rollbackFor = Exception.class)
    public void sellItemLast(String gnome_id, String item_id, BigDecimal itemPrice) throws Exception {

        try {
            String sqlTakeMoney = "update gnomes set gnome_money=gnome_money+?"
                    + " where gnome_id=?";
            jdbcTemplate.update(sqlTakeMoney, itemPrice, gnome_id);

//            error();

            String sqlDeleteSales = "delete from sales where item_id=? and gnome_id=?";
            jdbcTemplate.update(sqlDeleteSales, item_id, gnome_id);

        }catch (Exception dae){
            log.error("Error in creating record, rolling back");
            dae.printStackTrace();
            throw dae;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void sellItemOld(String gnome_id, String item_id, BigDecimal itemPrice) throws Exception {

        try {
            String sqlDecQuantity = "update sales set quantity=quantity-1 where gnome_id=?"
                    + " and item_id=?";
            jdbcTemplate.update(sqlDecQuantity, gnome_id, item_id);

//            error();

            String sqlTakeMoney = "update gnomes set gnome_money=gnome_money+?"
                    + " where gnome_id=?";
            jdbcTemplate.update(sqlTakeMoney, itemPrice, gnome_id);


        } catch (Exception dae) {
            log.error("Error in creating record, rolling back");
            dae.printStackTrace();
            throw dae;
        }
    }

}

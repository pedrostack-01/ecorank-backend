package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CatalogoAcaoRepository {

    private final DataSource dataSource;

    public CatalogoAcaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CatalogoAcao> findAll() {
        String sql = "SELECT ID, CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG FROM CATALOGO_ACAO ORDER BY ID";
        List<CatalogoAcao> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o catálogo de ações", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public CatalogoAcao findByCodigo(String codigo) {
        String sql = "SELECT ID, CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG FROM CATALOGO_ACAO WHERE CODIGO = ?";
        CatalogoAcao resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a ação do catálogo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    private CatalogoAcao montar(ResultSet rs) throws SQLException {
        CatalogoAcao x = new CatalogoAcao();
        x.setId(rs.getLong("ID"));
        x.setCodigo(rs.getString("CODIGO"));
        x.setRotulo(rs.getString("ROTULO"));
        x.setCategoria(rs.getString("CATEGORIA"));
        x.setPontos(rs.getInt("PONTOS"));
        x.setCo2Kg(rs.getDouble("CO2_KG"));
        return x;
    }

    public void save(CatalogoAcao x) {
        String sql = "INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES (?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, x.getCodigo());
            pstmt.setString(2, x.getRotulo());
            pstmt.setString(3, x.getCategoria());
            pstmt.setInt(4, x.getPontos());
            pstmt.setDouble(5, x.getCo2Kg());

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    x.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir a ação do catálogo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public CatalogoAcao findById(long id) {
        String sql = "SELECT ID, CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG FROM CATALOGO_ACAO WHERE ID = ?";
        CatalogoAcao resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a ação do catálogo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public void update(CatalogoAcao x) {
        String sql = "UPDATE CATALOGO_ACAO SET CODIGO = ?, ROTULO = ?, CATEGORIA = ?, PONTOS = ?, CO2_KG = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, x.getCodigo());
            pstmt.setString(2, x.getRotulo());
            pstmt.setString(3, x.getCategoria());
            pstmt.setInt(4, x.getPontos());
            pstmt.setDouble(5, x.getCo2Kg());
            pstmt.setLong(6, x.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a ação do catálogo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM CATALOGO_ACAO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a ação do catálogo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}

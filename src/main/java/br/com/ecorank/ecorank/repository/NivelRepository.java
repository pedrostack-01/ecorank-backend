package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.Nivel;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NivelRepository {

    private final DataSource dataSource;

    public NivelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Nivel nivel) {
        String sql = "INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) "
                + "VALUES (?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, nivel.getNome());
            pstmt.setInt(2, nivel.getPontosNecessarios());
            pstmt.setDouble(3, nivel.getMultiplicador());

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    nivel.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir o nível", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<Nivel> findAll() {
        String sql = "SELECT ID, NOME, PONTOS_NECESSARIOS, MULTIPLICADOR "
                + "FROM NIVEL ORDER BY PONTOS_NECESSARIOS";
        List<Nivel> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montarNivel(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando os níveis", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Nivel findById(long id) {
        String sql = "SELECT ID, NOME, PONTOS_NECESSARIOS, MULTIPLICADOR "
                + "FROM NIVEL WHERE ID = ?";
        Nivel nivel = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nivel = montarNivel(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o nível", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return nivel;
    }

    public void update(Nivel nivel) {
        String sql = "UPDATE NIVEL SET NOME = ?, PONTOS_NECESSARIOS = ?, "
                + "MULTIPLICADOR = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, nivel.getNome());
            pstmt.setInt(2, nivel.getPontosNecessarios());
            pstmt.setDouble(3, nivel.getMultiplicador());
            pstmt.setLong(4, nivel.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o nível", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM NIVEL WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o nível", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Nivel montarNivel(ResultSet rs) throws SQLException {
        Nivel nivel = new Nivel();
        nivel.setId(rs.getLong("ID"));
        nivel.setNome(rs.getString("NOME"));
        nivel.setPontosNecessarios(rs.getInt("PONTOS_NECESSARIOS"));
        nivel.setMultiplicador(rs.getDouble("MULTIPLICADOR"));
        return nivel;
    }
}

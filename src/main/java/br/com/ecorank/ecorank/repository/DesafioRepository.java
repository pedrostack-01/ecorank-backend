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
public class DesafioRepository {

    private final DataSource dataSource;

    public DesafioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Desafio> findAtivos() {
        String sql = "SELECT ID, TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES FROM DESAFIO WHERE ATIVO = 1 ORDER BY ID";
        List<Desafio> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montarDesafio(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando os desafios", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Desafio findById(long id) {
        String sql = "SELECT ID, TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES FROM DESAFIO WHERE ID = ? AND ATIVO = 1";
        Desafio resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montarDesafio(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o desafio", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public List<ParticipacaoDesafio> findParticipacoes(long idUsuario) {
        String sql = "SELECT ID, ID_USUARIO, ID_DESAFIO, DATA_INICIO, CONCLUIDO FROM PARTICIPACAO_DESAFIO WHERE ID_USUARIO = ?";
        List<ParticipacaoDesafio> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montarParticipacao(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as participações", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public void save(ParticipacaoDesafio x) {
        String sql = "INSERT INTO PARTICIPACAO_DESAFIO (ID_USUARIO, ID_DESAFIO, DATA_INICIO, CONCLUIDO) "
                + "VALUES (?, ?, ?, 0)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, x.getIdUsuario());
            pstmt.setLong(2, x.getIdDesafio());
            pstmt.setDate(3, Date.valueOf(x.getDataInicio()));

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
            throw new RuntimeException("Erro ao registrar a participação", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void concluir(long idParticipacao) {
        String sql = "UPDATE PARTICIPACAO_DESAFIO SET CONCLUIDO = 1, DATA_CONCLUSAO = SYSDATE WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idParticipacao);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao concluir o desafio", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Desafio montarDesafio(ResultSet rs) throws SQLException {
        Desafio x = new Desafio();
        x.setId(rs.getLong("ID"));
        x.setTitulo(rs.getString("TITULO"));
        x.setDescricao(rs.getString("DESCRICAO"));
        x.setRecompensa(rs.getInt("RECOMPENSA"));
        x.setCategoria(rs.getString("CATEGORIA"));  // NULL = qualquer categoria
        x.setMetaAcoes(rs.getInt("META_ACOES"));
        return x;
    }

    private ParticipacaoDesafio montarParticipacao(ResultSet rs) throws SQLException {
        ParticipacaoDesafio x = new ParticipacaoDesafio();
        x.setId(rs.getLong("ID"));
        x.setIdUsuario(rs.getLong("ID_USUARIO"));
        x.setIdDesafio(rs.getLong("ID_DESAFIO"));
        x.setDataInicio(rs.getDate("DATA_INICIO").toLocalDate());
        x.setConcluido(rs.getInt("CONCLUIDO") == 1);
        return x;
    }

    public void save(Desafio x) {
        String sql = "INSERT INTO DESAFIO (TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES) VALUES (?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, x.getTitulo());
            pstmt.setString(2, x.getDescricao());
            pstmt.setInt(3, x.getRecompensa());
            pstmt.setString(4, x.getCategoria());
            pstmt.setInt(5, x.getMetaAcoes());

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
            throw new RuntimeException("Erro ao inserir o desafio", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void update(Desafio x) {
        String sql = "UPDATE DESAFIO SET TITULO = ?, DESCRICAO = ?, RECOMPENSA = ?, CATEGORIA = ?, META_ACOES = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, x.getTitulo());
            pstmt.setString(2, x.getDescricao());
            pstmt.setInt(3, x.getRecompensa());
            pstmt.setString(4, x.getCategoria());
            pstmt.setInt(5, x.getMetaAcoes());
            pstmt.setLong(6, x.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o desafio", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM DESAFIO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o desafio", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}

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
public class ComprovacaoRepository {

    private final DataSource dataSource;

    public ComprovacaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Grava os dados e o conteúdo do arquivo (coluna BLOB) juntos
    public void save(Comprovacao x, byte[] conteudo) {
        String sql = "INSERT INTO COMPROVACAO (ID_ACAO, NOME_ORIGINAL, TIPO, TAMANHO, ARQUIVO, DURACAO_SEG, CONTEUDO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, x.getIdAcao());
            pstmt.setString(2, x.getNomeOriginal());
            pstmt.setString(3, x.getTipo());
            pstmt.setLong(4, x.getTamanho());
            pstmt.setString(5, x.getArquivo());
            pstmt.setInt(6, x.getDuracaoSeg());
            pstmt.setBytes(7, conteudo);

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
            throw new RuntimeException("Erro ao inserir a comprovação", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    // Retorna os bytes do arquivo (ou null se não existir)
    public byte[] findConteudo(long id) {
        String sql = "SELECT CONTEUDO FROM COMPROVACAO WHERE ID = ?";
        byte[] resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = rs.getBytes("CONTEUDO");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o arquivo da comprovação", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Comprovacao findByArquivo(String arquivo) {
        String sql = "SELECT ID, ID_ACAO, NOME_ORIGINAL, TIPO, TAMANHO, ARQUIVO, DURACAO_SEG FROM COMPROVACAO WHERE ARQUIVO = ?";
        Comprovacao resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, arquivo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a comprovação", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    private Comprovacao montar(ResultSet rs) throws SQLException {
        Comprovacao x = new Comprovacao();
        x.setId(rs.getLong("ID"));
        x.setIdAcao(rs.getLong("ID_ACAO"));
        x.setNomeOriginal(rs.getString("NOME_ORIGINAL"));
        x.setTipo(rs.getString("TIPO"));
        x.setTamanho(rs.getLong("TAMANHO"));
        x.setArquivo(rs.getString("ARQUIVO"));
        x.setDuracaoSeg(rs.getInt("DURACAO_SEG"));
        return x;
    }
}

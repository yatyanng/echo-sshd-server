package com.example.sshd.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class JdbcService {

	private static final String createRemoteIpLookupTableSql = "CREATE TABLE IF NOT EXISTS public.remote_ip_lookup (id BIGINT not null, "
			+ "remote_ip_address CHARACTER VARYING not null, remote_ip_info CHARACTER VARYING not null, PRIMARY KEY (id));";
	private static final String createRemoteIpLookupIndexSql = "CREATE INDEX IF NOT EXISTS public.remote_ip_lookup_idx ON "
			+ "public.remote_ip_lookup (remote_ip_address);";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	private void init() {
		jdbcTemplate.execute(createRemoteIpLookupTableSql);
		jdbcTemplate.execute(createRemoteIpLookupIndexSql);
	}

	public List<Map<String, Object>> getRemoteIpInfo(String remoteIp) {
		return jdbcTemplate.query(
				"SELECT id, remote_ip_address, remote_ip_info from public.remote_ip_lookup WHERE remote_ip_address = ? ",
				new RowMapper<Map<String, Object>>() {
					@Override
					public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
						return Map.of("id", rs.getLong(1), "remote_ip_address", rs.getString(2), "remote_ip_info",
								rs.getString(3));
					}
				}, remoteIp);
	}

	public int insertRemoteIpInfo(String remoteIpAddress, String remoteIpInfo) {
		return jdbcTemplate.update(
				"INSERT INTO public.remote_ip_lookup (id, remote_ip_address, remote_ip_info) VALUES (?, ?, ?)",
				System.currentTimeMillis(), remoteIpAddress, remoteIpInfo);
	}
}

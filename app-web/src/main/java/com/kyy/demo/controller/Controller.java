package com.kyy.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class Controller {

	static final Logger logger = LoggerFactory.getLogger(Controller.class);
	/**
	 * HTTP 200
	 */
	protected static final ResponseEntity<?> OK = new ResponseEntity<>(HttpStatus.OK);
	/**
	 * HTTP 201
	 */
	protected static final ResponseEntity<?> CREATED = new ResponseEntity<>(HttpStatus.CREATED);
	/**
	 * HTTP 201
	 */
	protected static final ResponseEntity<?> UPDATED = new ResponseEntity<>(HttpStatus.CREATED);
	/**
	 * HTTP 204
	 */
	protected static final ResponseEntity<?> DELETED = new ResponseEntity<>(HttpStatus.NO_CONTENT);
	/**
	 * HTTP 400
	 */
	protected static final ResponseEntity<?> BAD_REQUEST = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	/**
	 * HTTP 401
	 */
	protected static final ResponseEntity<?> UNAUTHORIZED = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	/**
	 * HTTP 403
	 */
	protected static final ResponseEntity<?> FORBIDDEN = new ResponseEntity<>(HttpStatus.FORBIDDEN);
	/**
	 * HTTP 404
	 */
	protected static final ResponseEntity<?> NOT_FOUND = new ResponseEntity<>(HttpStatus.NOT_FOUND);
	/**
	 * HTTP 409
	 */
	protected static final ResponseEntity<?> CONFLICT = new ResponseEntity<>(HttpStatus.CONFLICT);
	/**
	 * HTTP 500
	 */
	protected static final ResponseEntity<?> INTERNAL_SERVER_ERROR = new ResponseEntity<>(
			HttpStatus.INTERNAL_SERVER_ERROR);

	/**
	 * 统一异常处理
	 */
	@ExceptionHandler(Throwable.class)
	public ResponseEntity<?> handleException(Throwable e) {
		if (e.getCause() != null && e.getCause() instanceof NumberFormatException) {
			logger.error(e.getMessage(), e);
			return BAD_REQUEST;
		} else {
			logger.error(e.getMessage(), e);
			return INTERNAL_SERVER_ERROR;
		}
	}

}

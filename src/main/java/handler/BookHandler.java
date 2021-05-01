package handler;

import api.Handler;
import api.Method;
import api.Response;
import api.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.GlobalExceptionHandler;
import model.product.book.Book;
import service.BookService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static api.QueryParamName.PRODUCT_ID;
import static util.Constants.APPLICATION_JSON;
import static util.Constants.CONTENT_TYPE;
import static util.Utils.splitQuery;

public class BookHandler extends Handler {

    private final BookService bookService;

    public BookHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler, BookService service) {
        super(objectMapper, exceptionHandler);
        this.bookService = service;
    }

    @Override
    protected void execute(HttpExchange exchange) throws Exception {
        byte[] response = resolveRequest(exchange);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private byte[] resolveRequest(HttpExchange exchange) throws IOException {
        return handleBookRequests(exchange, splitQuery(exchange.getRequestURI().getRawQuery()));
    }

    private byte[] handleBookRequests(HttpExchange exchange, Map<String, String> params) throws IOException {
        byte[] response = null;
        Method method = Method.valueOf(exchange.getRequestMethod());
        switch (method) {
            case GET:
                Response<Book> get = handleGet(params);
                response = getResponseBodyAsBytes(get, exchange);
                break;
            case POST:
                Response<String> post = handlePost(exchange);
                response = getResponseBodyAsBytes(post, exchange);
                break;
            case DELETE:
                Response<Object> delete = handleDelete(params);
                response = getResponseBodyAsBytes(delete, exchange);
                break;
            default:
                throw new BadRequestException("Couldn't resolve a HTTP method");
        }
        return response;
    }

    private <T> byte[] getResponseBodyAsBytes(Response<T> response, HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().putAll(response.getHeaders());
        exchange.sendResponseHeaders(response.getStatus().getCode(), 0);
        return super.writeResponse(response.getBody());
    }

    private Response<Book> handleGet(Map<String, String> params) {
        String bookId = params.get(PRODUCT_ID.getName());
        return Response.<Book>builder()
                .headers(getHeaders(CONTENT_TYPE, APPLICATION_JSON))
                .body(bookService.get(bookId))
                .status(StatusCode.OK)
                .build();
    }

    private Response<String> handlePost(HttpExchange exchange) {
        Book book = readRequestBody(exchange.getRequestBody(), Book.class);
        String newId = bookService.create(book);
        return Response.<String>builder()
                .headers(getHeaders(CONTENT_TYPE, APPLICATION_JSON))
                .status(StatusCode.CREATED)
                .body(newId)
                .build();
    }

    private Response<Object> handleDelete(Map<String, String> params) {
        String id = params.get(PRODUCT_ID.getName());
        bookService.delete(id);
        return Response.builder()
                .status(StatusCode.NO_CONTENT)
                .build();
    }
}


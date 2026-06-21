import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Servicio centralizado para comunicación HTTP con la API del backend.
 * Se encarga de:
 * - Gestionar la URL base (localhost:8080/api)
 * - Exponer métodos GET, POST, PUT, DELETE tipados
 * - Servir como punto de inyección único para la capa HTTP
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly urlBase = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Realizar una petición GET genérica.
   * @param ruta Ruta del endpoint (ej: /productos, /stock/1)
   * @returns Observable con los datos retornados
   */
  obtener<T>(ruta: string): Observable<T> {
    return this.http.get<T>(`${this.urlBase}${ruta}`);
  }

  /**
   * Realizar una petición POST para crear un recurso.
   * @param ruta Ruta del endpoint (ej: /productos)
   * @param datos Objeto a enviar al servidor
   * @returns Observable con el recurso creado
   */
  crear<T>(ruta: string, datos: any): Observable<T> {
    return this.http.post<T>(`${this.urlBase}${ruta}`, datos);
  }

  /**
   * Realizar una petición PUT para actualizar un recurso.
   * @param ruta Ruta del endpoint (ej: /productos/1)
   * @param datos Objeto actualizado a enviar
   * @returns Observable con el recurso actualizado
   */
  actualizar<T>(ruta: string, datos: any): Observable<T> {
    return this.http.put<T>(`${this.urlBase}${ruta}`, datos);
  }

  /**
   * Realizar una petición DELETE para eliminar un recurso.
   * @param ruta Ruta del endpoint (ej: /productos/1)
   * @returns Observable con la respuesta del servidor
   */
  eliminar<T>(ruta: string): Observable<T> {
    return this.http.delete<T>(`${this.urlBase}${ruta}`);
  }
}

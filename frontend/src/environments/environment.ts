export const environment = {
  production: false,
  get apiUrl() {
    const hostname = window.location.hostname;
    // If accessing via localhost, use localhost
    // If accessing via IP, use that IP
    return `http://${hostname === 'localhost' ? 'localhost' : hostname}:8080/api`;
  }
};
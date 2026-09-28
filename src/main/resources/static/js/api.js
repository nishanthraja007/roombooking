/* REST API Client for BookMyRoom */
const API_BASE = '/api';

async function apiRequest(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        // 204 No Content — DELETE success, no body to parse
        if (response.status === 204) {
            return null;
        }

        const data = await response.json();

        if (!response.ok) {
            const error = new Error(data.message || 'An error occurred during API call');
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    } catch (err) {
        console.error(`API Error [${endpoint}]:`, err);
        throw err;
    }
}

export const API = {
    // Rooms
    getRooms: () => apiRequest('/rooms'),
    getRoomById: (id) => apiRequest(`/rooms/${id}`),
    createRoom: (roomData) => apiRequest('/rooms', { method: 'POST', body: JSON.stringify(roomData) }),
    updateRoom: (id, roomData) => apiRequest(`/rooms/${id}`, { method: 'PUT', body: JSON.stringify(roomData) }),
    deleteRoom: (id) => apiRequest(`/rooms/${id}`, { method: 'DELETE' }),
    getAvailableRooms: (fromDate, toDate) => apiRequest(`/rooms/available?fromDate=${fromDate}&toDate=${toDate}`),

    // Guests
    getGuests: () => apiRequest('/guests'),
    getGuestById: (id) => apiRequest(`/guests/${id}`),
    createGuest: (guestData) => apiRequest('/guests', { method: 'POST', body: JSON.stringify(guestData) }),

    // Bookings
    getBookings: () => apiRequest('/bookings'),
    getBookingById: (id) => apiRequest(`/bookings/${id}`),
    createBooking: (bookingData) => apiRequest('/bookings', { method: 'POST', body: JSON.stringify(bookingData) }),
    cancelBooking: (id) => apiRequest(`/bookings/${id}/cancel`, { method: 'PUT' }),
    getBookingsForMonth: (year, month) => apiRequest(`/bookings/month?year=${year}&month=${month}`),

    // Stats
    getDashboardStats: () => apiRequest('/dashboard/stats')
};

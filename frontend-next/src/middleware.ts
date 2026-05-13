import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { jwtVerify } from 'jose';

// Helper to decode Base64 to Uint8Array (Edge compatible)
function base64ToUint8Array(base64: string) {
  const binaryString = atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes;
}

const JWT_SECRET = process.env.JWT_SECRET || '';
const secret = base64ToUint8Array(JWT_SECRET);

const protectedRoutes = ['/dashboard', '/profile', '/settings', '/learning', '/jobs', '/finance', '/habits', '/social'];
const authRoutes = ['/login', '/signup'];

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  
  // Check if the route is protected
  const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route));
  const isAuthRoute = authRoutes.some(route => pathname.startsWith(route));

  const accessToken = request.cookies.get('access_token')?.value;
  const refreshToken = request.cookies.get('refresh_token')?.value;

  if (isProtectedRoute) {
    if (!accessToken && !refreshToken) {
      const url = new URL('/login', request.url);
      url.searchParams.set('callbackUrl', pathname);
      return NextResponse.redirect(url);
    }

    if (accessToken) {
      try {
        await jwtVerify(accessToken, secret);
        return NextResponse.next();
      } catch (error) {
        console.warn('Middleware: Access token verification failed', error);
        // If access token is invalid but refresh token exists, allow the request
        // so that the client-side interceptor can attempt a refresh.
        if (refreshToken) {
          return NextResponse.next();
        }
        const url = new URL('/login', request.url);
        url.searchParams.set('callbackUrl', pathname);
        return NextResponse.redirect(url);
      }
    }

    if (refreshToken) {
      // Access token missing but refresh token exists - allow through for client-side refresh
      return NextResponse.next();
    }
  }

  if (isAuthRoute && accessToken) {
    try {
      await jwtVerify(accessToken, secret);
      // Redirect to dashboard if already logged in
      return NextResponse.redirect(new URL('/dashboard', request.url));
    } catch (error) {
      // Token invalid, allow access to auth routes
      return NextResponse.next();
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};

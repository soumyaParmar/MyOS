import { cookies } from 'next/headers';
import { jwtVerify } from 'jose';

// Helper to decode Base64 to Uint8Array
function base64ToUint8Array(base64: string) {
  if (typeof btoa === 'undefined') {
    return Buffer.from(base64, 'base64');
  }
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

export interface ServerUser {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

export async function getServerSession(): Promise<ServerUser | null> {
  try {
    const cookieStore = await cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      return null;
    }

    const { payload } = await jwtVerify(accessToken, secret);
    
    // Convert backend payload to our User interface
    const roles = Array.isArray(payload.roles) 
      ? payload.roles 
      : typeof payload.roles === 'string' 
        ? payload.roles.split(',') 
        : [];

    return {
      id: (payload.userId as string) || (payload.sub as string),
      name: (payload.name as string) || 'User',
      email: payload.sub as string,
      roles: roles,
    };
  } catch (error) {
    // We don't log errors here as it might be common (token expired)
    return null;
  }
}

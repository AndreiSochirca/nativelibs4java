package org.bridj;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

interface CallIO {
	Object newInstance(long address);
	void checkArg(Object arg);

    public static class Utils {
        public static CallIO createPointerCallIOToTargetType(Type targetType) {
            return new CallIO.GenericPointerHandler(targetType);
        }
        //static final Map<Type, CallIO> instances = new HashMap<Type, CallIO>();
        public static /*synchronized*/ CallIO createPointerCallIO(Type type) {
            //CallIO io = instances.get(type);
            //if (io == null)
            //    instances.put(type, io = )
            return createPointerCallIO(org.bridj.util.Utils.getClass(type), type);
        }
        public static CallIO createPointerCallIO(Class<?> cl, Type type) {
            if (cl == Pointer.class)
                return createPointerCallIOToTargetType((type instanceof ParameterizedType) ? ((ParameterizedType)type).getActualTypeArguments()[0] : null);

            assert TypedPointer.class.isAssignableFrom(cl);
            return new CallIO.TypedPointerIO(((Class<? extends TypedPointer>)cl));
        }
    }
	
	public static class TypedPointerIO implements CallIO {
		Class<? extends TypedPointer> type;
		Constructor<?> constructor;
		public TypedPointerIO(Class<? extends TypedPointer> type) {
			this.type = type;
			try {
				this.constructor = type.getConstructor(long.class);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to create " + CallIO.class.getName() + " for type " + type.getName(), ex);
			}
		}
		//@Override
		public Pointer<?> newInstance(long address) {
			try {
				return (Pointer<?>) constructor.newInstance(address);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to instantiate pointer of type " + type.getName(), ex);
			}
		}
		//@Override
		public void checkArg(Object ptr) {
			type.cast(ptr);
		}
	}

	public static class NativeObjectHandler implements CallIO {
		Class<? extends NativeObject> nativeClass;
		Type nativeType;
		public NativeObjectHandler(Class<? extends NativeObject> type, Type t) {
			this.nativeClass = type;
			this.nativeType = t;
		}
		//@Override
		public NativeObject newInstance(long address) {
			return Pointer.pointerToAddress(address).getNativeObject(nativeClass);
		}
		//@Override
		public void checkArg(Object ptr) {
			if (ptr == null)
				throw new IllegalArgumentException("Native object of type " + nativeClass.getName() + " passed by value cannot be given a null value !");
		}
	}
	
	public static class GenericPointerHandler implements CallIO {
		Type targetType;
		PointerIO pointerIO;
		public GenericPointerHandler(Type targetType) {
			this.targetType = targetType;
			this.pointerIO = PointerIO.getInstance(targetType);
		}
		//@Override
		public Pointer<?> newInstance(long address) {
			return Pointer.pointerToAddress(address, pointerIO);
		}
		//@Override
		public void checkArg(Object ptr) {
			//Pointer<?> pointer = (Pointer<?>)ptr;
			//if (pointer.getIO() == null)
			//	pointer.setIO(pointerIO);
			// TODO check existing pointerio !
		}
	}
}
